// AUTO-GENERATED FILE. DO NOT MODIFY.

// This file is an auto-generated file by Ballerina persistence layer for entities.
// It should not be modified by hand.

import ballerina/persist;
import ballerina/sql;
import ballerinax/mysql;

const COMPANY = "company";
const EMPLOYEE = "employee";

public client class EntitiesClient {
    *persist:AbstractPersistClient;

    private final mysql:Client dbClient;

    private final map<persist:SQLClient> persistClients;

    private final record {|persist:Metadata...;|} metadata = {
        "company": {
            entityName: "Company",
            tableName: `Company`,
            fieldMetadata: {
                id: {columnName: "id", 'type: int},
                name: {columnName: "name", 'type: string}
            },
            keyFields: ["id"]
        },
        "employee": {
            entityName: "Employee",
            tableName: `Employee`,
            fieldMetadata: {
                id: {columnName: "id", 'type: int},
                name: {columnName: "name", 'type: string},
                companyId: {columnName: "companyId", 'type: int}
            },
            keyFields: ["id"]
        }
    };

    public function init() returns persist:Error? {
        mysql:Client|error dbClient = new (host = host, user = user, password = password, database = database, port = port);
        if dbClient is error {
            return <persist:Error>error(dbClient.message());
        }
        self.dbClient = dbClient;
        self.persistClients = {
            company: check new (self.dbClient, self.metadata.get(COMPANY)),
            employee: check new (self.dbClient, self.metadata.get(EMPLOYEE))
        };
    }

    isolated resource function get company() returns stream<Company, persist:Error?> {
        stream<record {}, sql:Error?>|persist:Error result = self.persistClients.get(COMPANY).runReadQuery(Company);
        if result is persist:Error {
            return new stream<Company, persist:Error?>(new CompanyStream((), result));
        } else {
            return new stream<Company, persist:Error?>(new CompanyStream(result));
        }
    }

    isolated resource function get company/[int id]() returns Company|persist:Error {
        Company|error result = (check self.persistClients.get(COMPANY).runReadByKeyQuery(Company, id)).cloneWithType(Company);
        if result is error {
            return <persist:Error>error(result.message());
        }
        return result;
    }

    isolated resource function post company(CompanyInsert[] data) returns int[]|persist:Error {
        _ = check self.persistClients.get(COMPANY).runBatchInsertQuery(data);
        return from CompanyInsert inserted in data
            select inserted.id;
    }

    isolated resource function put company/[int id](CompanyUpdate value) returns Company|persist:Error {
        _ = check self.persistClients.get(COMPANY).runUpdateQuery({"id": id}, value);
        return self->/company/[id].get();
    }

    isolated resource function delete company/[int id]() returns Company|persist:Error {
        Company 'object = check self->/company/[id].get();
        _ = check self.persistClients.get(COMPANY).runDeleteQuery({"id": id});
        return 'object;
    }

    isolated resource function get employee() returns stream<Employee, persist:Error?> {
        stream<record {}, sql:Error?>|persist:Error result = self.persistClients.get(EMPLOYEE).runReadQuery(Employee);
        if result is persist:Error {
            return new stream<Employee, persist:Error?>(new EmployeeStream((), result));
        } else {
            return new stream<Employee, persist:Error?>(new EmployeeStream(result));
        }
    }

    isolated resource function get employee/[int id]() returns Employee|persist:Error {
        Employee|error result = (check self.persistClients.get(EMPLOYEE).runReadByKeyQuery(Employee, id)).cloneWithType(Employee);
        if result is error {
            return <persist:Error>error(result.message());
        }
        return result;
    }

    isolated resource function post employee(EmployeeInsert[] data) returns int[]|persist:Error {
        _ = check self.persistClients.get(EMPLOYEE).runBatchInsertQuery(data);
        return from EmployeeInsert inserted in data
            select inserted.id;
    }

    isolated resource function put employee/[int id](EmployeeUpdate value) returns Employee|persist:Error {
        _ = check self.persistClients.get(EMPLOYEE).runUpdateQuery({"id": id}, value);
        return self->/employee/[id].get();
    }

    isolated resource function delete employee/[int id]() returns Employee|persist:Error {
        Employee 'object = check self->/employee/[id].get();
        _ = check self.persistClients.get(EMPLOYEE).runDeleteQuery({"id": id});
        return 'object;
    }

    public function close() returns persist:Error? {
        error? result = self.dbClient.close();
        if result is error {
            return <persist:Error>error(result.message());
        }
        return result;
    }
}

public class CompanyStream {

    private stream<anydata, sql:Error?>? anydataStream;
    private persist:Error? err;

    public isolated function init(stream<anydata, sql:Error?>? anydataStream, persist:Error? err = ()) {
        self.anydataStream = anydataStream;
        self.err = err;
    }

    public isolated function next() returns record {|Company value;|}|persist:Error? {
        if self.err is persist:Error {
            return <persist:Error>self.err;
        } else if self.anydataStream is stream<anydata, sql:Error?> {
            var anydataStream = <stream<anydata, sql:Error?>>self.anydataStream;
            var streamValue = anydataStream.next();
            if streamValue is () {
                return streamValue;
            } else if (streamValue is sql:Error) {
                return <persist:Error>error(streamValue.message());
            } else {
                Company|error value = streamValue.value.cloneWithType(Company);
                if value is error {
                    return <persist:Error>error(value.message());
                }
                record {|Company value;|} nextRecord = {value: value};
                return nextRecord;
            }
        } else {
            return ();
        }
    }

    public isolated function close() returns persist:Error? {
        check persist:closeEntityStream(self.anydataStream);
    }
}

public class EmployeeStream {

    private stream<anydata, sql:Error?>? anydataStream;
    private persist:Error? err;

    public isolated function init(stream<anydata, sql:Error?>? anydataStream, persist:Error? err = ()) {
        self.anydataStream = anydataStream;
        self.err = err;
    }

    public isolated function next() returns record {|Employee value;|}|persist:Error? {
        if self.err is persist:Error {
            return <persist:Error>self.err;
        } else if self.anydataStream is stream<anydata, sql:Error?> {
            var anydataStream = <stream<anydata, sql:Error?>>self.anydataStream;
            var streamValue = anydataStream.next();
            if streamValue is () {
                return streamValue;
            } else if (streamValue is sql:Error) {
                return <persist:Error>error(streamValue.message());
            } else {
                Employee|error value = streamValue.value.cloneWithType(Employee);
                if value is error {
                    return <persist:Error>error(value.message());
                }
                record {|Employee value;|} nextRecord = {value: value};
                return nextRecord;
            }
        } else {
            return ();
        }
    }

    public isolated function close() returns persist:Error? {
        check persist:closeEntityStream(self.anydataStream);
    }
}

