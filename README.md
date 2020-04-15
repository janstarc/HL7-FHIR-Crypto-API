# HL7 FHIR Resource Encryptor/Decryptor Microservice API
## 0. Build
- Project is built with Maven, **pom.xml** is in the root folder.
- Last version was tested with Tomcat 9.0.4. 
    - HTTP port: 7050
    - JMX port: 1999
- All addresses are defined in **com.diplomska.constants --> address.java**
- This solution needs MySQL database in order to work. SQL file (DB structure and INSERT statements of test data) is in **com.diplomska.testniPrimeri --> db.sql** file.

# Features
## 1. POST resources
### 1.1 Patient
**POST** request with a valid FHIR Patient resource (in JSON format) to http://localhost:7050/hapi.do/Patient \
*or*\
**testniPrimeri.java --> addPatient()** (Creates a sample FHIR Patient resource (in JSON format) and sends the POST request to the address above)

### 1.2 Observation
**POST** request with a valid FHIR Observation resource (in JSON format, with correctly added reference extension) to http://localhost:7050/hapi.do/Observation \
*or*\
**testniPrimeri.java --> addObservationToPatient()** (Creates a sample FHIR Observation resource (in JSON format) and sends the POST request to the address above. 
Patient reference is encrypted before sending the Observation to the server. So the server never gets a non-encrypted version of the Patient reference).

### 1.3 Condition
**POST** request with a valid FHIR Condition resource (in JSON format, with correctly added reference extension) to http://localhost:7050/hapi.do/Condition \
*or*\
**testniPrimeri.java --> addConditionToPatient()** (Creates a sample FHIR Condition resource (in JSON format) and sends the POST request to the address above. 
Patient reference is encrypted before sending the Condition to the server. So the server never gets a non-encrypted version of the Patient reference).

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
