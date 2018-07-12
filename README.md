# Diplomska
## 0. Build
- Projekt se builda z Mavenom, pom.xml je v root mapi.
- Testiral sem s Tomcatom 9.0.4. 
    - HTTP port: 7050
    - JMX port: 1999
- Vsi naslovi so definirani v **com.diplomska.constants --> address.java**
- Dodana je MySQL podatkovna baza. SQL datoteka (struktura tabel in inserti) je v mapi **com.diplomska.testniPrimeri --> db.sql**

## 1. Kreiranje resourcev
### 1.1 Patient
**POST** z veljavnim FHIR JSON objektom na http://localhost:7050/hapi.do/Patient \
*ali*\
**testniPrimeri.java --> addPatient()** (Kreira JSON resource in pošlje POST request na zgornji naslov)

### 1.2 Observation
**POST** z veljavnim FHIR JSON objektom (s pravilno dodanim extensionom z referenco) na http://localhost:7050/hapi.do/Observation \
*ali*\
**testniPrimeri.java --> addObservationToPatient()** (Kreira JSON resource in pošlje POST request na zgornji naslov, 
nato se kriptira referenca na pacienta in se v kripitrani obliki shrani na strežnik)

### 1.3 Condition
**POST** z veljavnim FHIR JSON objektom (s pravilno dodanim extensionom z referenco) na http://localhost:7050/hapi.do/Condition \
*ali*\
**testniPrimeri.java --> addConditionToPatient()** (Kreira JSON resource in pošlje POST request na zgornji naslov, 
nato se kriptira referenca na pacienta in se v kripitrani obliki shrani na strežnik)

## 2. Iskanje resourcev
### 2.1 Patient
- Iskanje po IDju: **GET** request, npr. za pacienta z *_id = 100* http://localhost:7050/hapi.do/Patient?_id=100 \
*ali*\
**testniPrimeri.java --> getPatientById()**

- Iskanje po imenu in priimku: **GET** request, npr. za pacienta Testni Pacient http://localhost:7050/hapi.do/Patient?given=Testni&family=Pacient \
*ali*\
**testniPrimeri.java --> getPatientByGivenFamily()**

### 2.2 Observation
*V vseh resourcih je pri izvedbi GET requesta referenca na pacienta za clienta vidna v nekriptirani obliki, na HAPI strežniku je referenca shranjena v kripitirani obliki.*
- Iskanje po Patient IDju: **GET** request, npr. za pacienta z *_id = 100* http://localhost:7050/hapi.do/Observation?patient=100
*ali*\
**testniPrimeri.java --> getAllObservationsForPatient()**

### 2.3 Condition
*Glede kriptiranja/referenc rešeno enako kot pri Observationu*
- Iskanje po Patient IDju: **GET** request, npr. za pacienta z *_id = 100* http://localhost:7050/hapi.do/Condition?patient=100
*ali*\
**testniPrimeri.java --> getAllConditionsForPatient()**

## 3. Menjava ključev
- Nov ključ se lahko generira preko APIja. (http://localhost:7050/crypto.do/GenerateNewKey?keyAlias=imeKljuca).
- Vsi resourci enega pacienta so kriptirani pod istim ključem (trenutno je v KeyStoru 15 testnih ključev: key1 - key15).
- Menjavo ključa za vse observatione nekega pacienta se izvede kot transakcija 
    - Menjavo ključa za pacienta z id=20002 in ključ=key12 izvedemo kot: http://localhost:7050/hapi.do/ChangeKey?_id=20002&keyAlias=key12