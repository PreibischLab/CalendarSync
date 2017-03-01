mvn clean assembly:assembly -DdescriptorId=jar-with-dependencies
mv target/calsync-1-jar-with-dependencies.jar ./calsync.jar

echo
echo Created executable calsync-1.jar
echo Requires exchange.pwd and client_secred_microscopycalendar.json to run
echo 
echo Run with 'java -jar calsync.jar'
echo
