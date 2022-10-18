plugins {
    id 'java'
    id 'application'
}

group 'org.example'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.12'


    // AWS
    implementation platform('software.amazon.awssdk:bom:2.13.13')
    implementation 'software.amazon.awssdk:sqs'
    implementation 'software.amazon.awssdk:regions'
    implementation 'software.amazon.awssdk:auth'

    // Neo4j
    compile'org.neo4j.driver:neo4j-java-driver:4.1.1'

    // Gson
    implementation 'com.google.code.gson:gson:2.8.6'

    // MOEA
    compile 'org.moeaframework:moeaframework:2.12'

    // CSV
    compile 'com.opencsv:opencsv:5.2'

    // MATRIX OPERATIONS
    compile group: 'org.ejml', name: 'ejml-all', version: '0.40'
}



application {
    mainClassName = 'app.AppProd'
}