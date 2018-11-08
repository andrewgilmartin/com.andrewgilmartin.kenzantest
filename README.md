# com.andrewgilmartin.kenzantest

To run the REST API server you need to install Tomcat 8, create a Tomcat user, build the war, and deploy the WAR to Tomcat.

Installing Tomcat is done by downloading the distribution at

    https://tomcat.apache.org/download-80.cgi

and untar/unzip it into a directory, eg $HOME/tmp/.

The project has one secure operation that requires that Tomcat know of a user with the "MANAGER" role. For this example edit the file 

    $HOME/tmp/apache-tomcat-8.5.34/conf/tomcat-users.xml

and add the two lines

    <role rolename="MANAGER"/>
    <user username="kenzan" password="kenzan" roles="MANAGER"/>

Build the WAR. Maven is used to build the WAR

    mvn clean package
   
Start Tomcat and perhaps tail it output file

    tail -F $HOME/tmp/apache-tomcat-8.5.34/logs/catalina.out &
    
    $HOME/tmp/apache-tomcat-8.5.34/bin/catalina.sh jpda start
 
Deploy the WAR
 
     cp target/kenzantest-1.0-SNAPSHOT.war $HOME/tmp/apache-tomcat-8.5.34/webapps/kenzantest.war

To interact with the REST API use the following methods:

To view an employee provide the employee's id 

    curl -D - 'http://localhost:8080/kenzantest/58E3C945-3D2B-47F6-9983-D9CD03D7F143'
    
To view all employees

    curl -D - 'http://localhost:8080/kenzantest'

To add an employee provide the employee field details. The added employee details are returned.

    curl -D - -X PUT -H 'Content-Type: application/json' --data '{ "firstName":"Joe", "lastName":"Friday", "dateOfBirth":"1936-02-12" }' 'http://localhost:8080/kenzantest'

To update an employee provide the employee's id and the update field details. The updated employee details are returned.

    curl -D - -X POST -H 'Content-Type: application/json' --data '{ "firstName":"Henry", "dateOfBirth":"1999-11-19" }' 'http://localhost:8080/kenzantest/58E3C945-3D2B-47F6-9983-D9CD03D7F143'

To deactivate an employee provide the employee's id

    curl -D - -X DELETE --user kenzan:kenzan 'http://localhost:8080/kenzantest/58E3C945-3D2B-47F6-9983-D9CD03D7F143'

To deactivate all employees

    curl -D - -X DELETE --user kenzan:kenzan 'http://localhost:8080/kenzantest'

END
    

