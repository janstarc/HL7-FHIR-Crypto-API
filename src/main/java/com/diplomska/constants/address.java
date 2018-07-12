package com.diplomska.constants;

public class address {


    public static String HapiAccessPoint = "http://localhost:7050/hapi.do";
    public static String HapiCrypto = "http://localhost:7050/crypto.do";

    public static String HapiRESTfulServer = "http://hapi.fhir.org/baseDstu2";

    public static String HapiAccessPointPatient = HapiAccessPoint + "/Patient";
    public static String HapiAccessPointObservation = HapiAccessPoint + "/Observation";
    public static String HapiAccessPointCondition = HapiAccessPoint + "/Condition";
    public static String HapiCryptoObservation = HapiCrypto + "/Observation";
    public static String HapiCryptoCondition = HapiCrypto + "/Condition";
    public static String HapiCryptoChangeKey = HapiCrypto + "/ChangeKey";

    //public static String HapiPublicTestServer = "http://hapi.fhir.org/baseDstu2";
    //public static String HapiRESTfulServer = "http://localhost:8080/hapi/baseDstu2";
}
