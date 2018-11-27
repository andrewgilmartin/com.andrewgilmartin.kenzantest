## com.andrewgilmartin.kenzantest

As part of a job interview I was asked to implement a mini-project. This repository is my response. I did not get the job, but that outcome has little to do with this mini-project.

# Mini-project


Create a web application that exposes REST operations for employees. The API should be able to:

1. Get employees by an ID
2. Create new employees
3. Update existing employees
4. Delete employees
5. Get all employees

An employee is made up of the following data:

* Employee spec
* ID - Unique identifier for an employee
* FirstName - Employee first name
* MiddleInitial - Employee middle initial
* LastName - Employee last name
* DateOfBirth - Employee birthday and year
* DateOfEmployment - Employee start date
* Status - ACTIVE or INACTIVE

**Startup** 

On startup, the application should be able to ingest an external source of employees, and should make them available via the GET endpoint.

**ACTIVE vs INACTIVE employees** 

By default, all employees are active, but by way of the API, can be switched to inactive. This should be done by the delete API call. This call should require some manner of authorization header.

When an employee is inactive, they should no longer be able to be retrieved in either the get by id, or get all employees calls

# Response  

To run the employee server you need to install Tomcat 8, create a Tomcat user, build the war, and deploy the WAR to Tomcat.

Installing Tomcat is done by downloading the distribution at

    https://tomcat.apache.org/download-80.cgi

and untar/unzip it into a directory, eg $HOME/tmp/.

The employee server has one secure operation that requires that Tomcat know of a user with the "MANAGER" role. For this example edit the file 

    $HOME/tmp/apache-tomcat-8.5.34/conf/tomcat-users.xml

and add the two lines

    <role rolename="MANAGER"/>
    <user username="kenzan" password="kenzan" roles="MANAGER"/>

Build the employee server WAR. Maven is used to build the WAR

    git clone https://github.com/andrewgilmartin/com.andrewgilmartin.kenzantest.git
    cd com.andrewgilmartin.kenzantest
    
    mvn clean package
   
Start Tomcat and perhaps tail it output file

    tail -F $HOME/tmp/apache-tomcat-8.5.34/logs/catalina.out &
    
    $HOME/tmp/apache-tomcat-8.5.34/bin/catalina.sh jpda start
 
Deploy the WAR
 
     cp target/kenzantest-1.0-SNAPSHOT.war $HOME/tmp/apache-tomcat-8.5.34/webapps/kenzantest.war

To interact with the employee server use the following REST API operations:

To view an employee provide the employee's id 

    curl -D - 'http://localhost:8080/kenzantest/58E3C945-3D2B-47F6-9983-D9CD03D7F143'
    
To view all employees

    curl -D - 'http://localhost:8080/kenzantest'

To add an employee provide the employee field details. The added employee is returned.

    curl -D - -X PUT -H 'Content-Type: application/json' --data '{ "firstName":"Joe", "lastName":"Friday", "dateOfBirth":"1936-02-12" }' 'http://localhost:8080/kenzantest'

To update an employee provide the employee's id and the update field details. The updated employee is returned.

    curl -D - -X POST -H 'Content-Type: application/json' --data '{ "firstName":"Henry", "dateOfBirth":"1999-11-19" }' 'http://localhost:8080/kenzantest/58E3C945-3D2B-47F6-9983-D9CD03D7F143'

To deactivate an employee provide the employee's id. No content is returned.

    curl -D - -X DELETE --user kenzan:kenzan 'http://localhost:8080/kenzantest/58E3C945-3D2B-47F6-9983-D9CD03D7F143'

To deactivate all employees. No content is returned.

    curl -D - -X DELETE --user kenzan:kenzan 'http://localhost:8080/kenzantest'

END
