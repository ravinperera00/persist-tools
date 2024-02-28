/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.persist.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.persist.BalException;
import io.ballerina.persist.PersistToolsConstants;
import io.ballerina.persist.nodegenerator.syntax.constants.BalSyntaxConstants;
import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.persist.PersistToolsConstants.COMPONENT_IDENTIFIER;
import static io.ballerina.persist.PersistToolsConstants.MIGRATIONS;
import static io.ballerina.persist.PersistToolsConstants.PERSIST_DIRECTORY;
import static io.ballerina.persist.PersistToolsConstants.SUPPORTED_DB_PROVIDERS;
import static io.ballerina.persist.nodegenerator.syntax.constants.BalSyntaxConstants.BAL_EXTENSION;
import static io.ballerina.persist.nodegenerator.syntax.utils.TomlSyntaxUtils.readPackageName;
import static io.ballerina.persist.utils.BalProjectUtils.validateBallerinaProject;

/**
 * Class to implement "persist init" command for ballerina.
 *
 * @since 0.1.0
 */

@CommandLine.Command(
        name = "init",
        description = "Initialize the persistence layer in the Ballerina project.")

public class Init implements BLauncherCmd {

    private final PrintStream errStream = System.err;
    private static final String COMMAND_IDENTIFIER = "persist-init";
    private final String sourcePath;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--datastore"})
    private String datastore;

    @CommandLine.Option(names = {"--module"})
    private String module;

    public Init() {
        this("");
    }

    public Init(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(COMMAND_IDENTIFIER);
            errStream.println(commandUsageInfo);
            return;
        }

        if (datastore == null) {
            datastore = PersistToolsConstants.SupportedDataSources.IN_MEMORY_TABLE;
        } else if (!SUPPORTED_DB_PROVIDERS.contains(datastore)) {
            errStream.printf("ERROR: the persist layer supports one of data stores: %s" +
                    ". but found '%s' datasource.%n", Arrays.toString(SUPPORTED_DB_PROVIDERS.toArray()), datastore);
            return;
        }

        if (Files.isDirectory(Paths.get(sourcePath, PERSIST_DIRECTORY, MIGRATIONS))) {
            errStream.println("ERROR: reinitializing persistence after executing the migrate command is not " +
                    "permitted. please remove the migrations directory within the persist directory and try " +
                    "executing the command again.");
            return;
        }

        if (datastore.equals(PersistToolsConstants.SupportedDataSources.GOOGLE_SHEETS)) {
            errStream.printf(BalSyntaxConstants.EXPERIMENTAL_NOTICE, "The support for Google Sheets data store " +
                    "is currently an experimental feature, and its behavior may be subject to change in future " +
                    "releases." + System.lineSeparator());
        }

        Path projectPath = Paths.get(sourcePath);
        try {
            validateBallerinaProject(projectPath);
        } catch (BalException e) {
            errStream.println(e.getMessage());
            return;
        }
        String packageName;
        try {
            packageName = readPackageName(this.sourcePath);
        } catch (BalException e) {
            errStream.println(e.getMessage());
            return;
        }
        if (module == null) {
            module = packageName;
        } else {
            module = module.replaceAll("\"", "");
        }
        if (!ProjectUtils.validateModuleName(module)) {
            errStream.println("ERROR: invalid module name : '" + module + "' :\n" +
                    "module name can only contain alphanumerics, underscores and periods");
            return;
        } else if (!ProjectUtils.validateNameLength(module)) {
            errStream.println("ERROR: invalid module name : '" + module + "' :\n" +
                    "maximum length of module name is 256 characters");
            return;
        }

        Path persistDirPath = Paths.get(this.sourcePath, PERSIST_DIRECTORY);
        if (!Files.exists(persistDirPath)) {
            try {
                Files.createDirectory(persistDirPath.toAbsolutePath());
            } catch (IOException e) {
                errStream.println("ERROR: failed to create the persist directory. " + e.getMessage());
                return;
            }
        }
        List<String> schemaFiles;
        try (Stream<Path> stream = Files.list(persistDirPath)) {
            schemaFiles = stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .filter(Objects::nonNull)
                    .filter(file -> file.toString().toLowerCase(Locale.ENGLISH).endsWith(BAL_EXTENSION))
                    .map(file -> file.toString().replace(BAL_EXTENSION, ""))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            errStream.println("ERROR: failed to list model definition files in the persist directory. "
                    + e.getMessage());
            return;
        }
        if (schemaFiles.size() > 1) {
            errStream.println("ERROR: the persist directory allows only one model definition file, " +
                    "but contains many files.");
            return;
        }
        if (schemaFiles.size() == 0) {
            try {
                Utils.generateSchemaBalFile(persistDirPath);
            } catch (BalException e) {
                errStream.println("ERROR: failed to create the model definition file in persist directory. "
                        + e.getMessage());
                return;
            }
        }
        errStream.println("Initialized the package for persistence.");
        errStream.println(System.lineSeparator() + "Next steps:");

        errStream.println("- Define your data model in \"persist/model.bal\".");
        errStream.println("- Run \"bal persist generate\" to generate persist client and " +
                "entity types for your data model.");

    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
    @Override
    public String getName() {
        return COMPONENT_IDENTIFIER;
    }
    
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generate database configurations file inside the Ballerina project").append(System.lineSeparator());
        out.append(System.lineSeparator());
    }
    
    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append("  ballerina " + COMPONENT_IDENTIFIER +
                " init").append(System.lineSeparator());
    }
}
