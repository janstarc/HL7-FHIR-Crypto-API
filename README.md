# HL7 FHIR Resource Encryptor/Decryptor Microservice API
The project is described in detail in this [thesis](https://repozitorij.uni-lj.si/IzpisGradiva.php?id=103201&lang=eng) *(in Slovenian language)*

# Main goals and architecture
The main goal of the **HL7 FHIR Crypto API Microservice** is to ensure a safe transfer of HL7 FHIR resources from the app to FHIR  server, without the end-user experiencing any change in the use of the app. The Crypto API microservice is exposed outwards as a standard HL7 FHIR API interface.

The only change for the user is that access point for HAPI server is no longer the HAPI server itself, but instead the microservice in this repository. There is also no change in the database itself - all data is saved in the same way, no matter that some data on the DB is encrypted.

The goal is that the DB server stores encrypted data and never gets a decryption key. So, even if an attacker gets a full access to the DB, the data is useless, because the resources are impossible to be interpreted due to encrypted Patient reference.

## Architecture




# Features
## 0. Build
- Project is built with Maven, **pom.xml** is in the root folder.
- Last version was tested with Tomcat 9.0.4. 
    - HTTP port: 7050
    - JMX port: 1999
- All addresses are defined in **com.diplomska.constants --> address.java**
- This solution needs MySQL database in order to work. SQL file (DB structure and all INSERT statements for test data) is in **com.diplomska.testniPrimeri --> db.sql** file.

## 1. POST resources
### 1.1 Patient
**POST** request with a valid FHIR Patient resource (in JSON format) to http://localhost:7050/hapi.do/Patient \
*or*\
**testniPrimeri.java --> addPatient()** (Creates a sample FHIR Patient resource (in JSON format) and sends the POST request to the address above)

### 1.2 Observation
**POST** request with a valid FHIR Observation resource (in JSON format, with correctly added reference extension) to http://localhost:7050/hapi.do/Observation \
*or*\
**testniPrimeri.java --> addObservationToPatient()** (Creates a sample FHIR Observation resource (in JSON format) and sends the POST request to the address above. 
Patient reference is encrypted before sending the Observation to the server. The server never gets a non-encrypted version of the Patient reference).

### 1.3 Condition
**POST** request with a valid FHIR Condition resource (in JSON format, with correctly added reference extension) to http://localhost:7050/hapi.do/Condition \
*or*\
**testniPrimeri.java --> addConditionToPatient()** (Creates a sample FHIR Condition resource (in JSON format) and sends the POST request to the address above. 
Patient reference is encrypted before sending the Condition to the server. The server never gets a non-encrypted version of the Patient reference).

## 2. GET resources
### 2.1 Patient
- Search by ID: **GET** request, example for a patient with *_id = 100* http://localhost:7050/hapi.do/Patient?_id=100 \
*or*\
**testniPrimeri.java --> getPatientById()**

- Search by name and surname: **GET** request, example for a patient *John Doe* http://localhost:7050/hapi.do/Patient?given=John&family=Doe \
*ali*\
**testniPrimeri.java --> getPatientByGivenFamily()**

### 2.2 Observation
*When a resource (of type Observation or Condition) is requested by an authorized user, it contains a decrypted Patient reference. But on the HAPI server, a Patient reference is never saved in an unencrypted form* 
- Searching by Patient ID (get all Observations for a patient): **GET** request, example for a patient with *_id = 100* http://localhost:7050/hapi.do/Observation?patient=100\
*or*\
**testniPrimeri.java --> getAllObservationsForPatient()**

### 2.3 Condition
*When a resource (of type Observation or Condition) is requested by an authorized user, it contains a decrypted Patient reference. But on the HAPI server, a Patient reference is never saved in an unencrypted form* 
- Searching by Patient ID (get all Conditions for a patient): **GET** request, example for a patient with *_id = 100* http://localhost:7050/hapi.do/Condition?patient=100\
*or*\
**testniPrimeri.java --> getAllConditionsForPatient()**

## 3. Encryption key change
- A new encryption key can be generated with API (Example for a key with a name *sampleKey* http://localhost:7050/crypto.do/GenerateNewKey?keyAlias=sampleKey).
- All resources of some patient are encrypted with the *same* key. (Currently, there are 15 test keys in the KeyStore: key1 - key15).
- Key exchange for a patient (currently implemented for Observation and Condition resource) is executed as a transaction.
    - Example: Change key for a patient with *id=20002* from current key to *key12*: http://localhost:7050/hapi.do/ChangeKey?_id=20002&keyAlias=key12
