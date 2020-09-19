# PUCUM
A Java-based REST API to convert unis of measurements specified in PANGAEA datasets into UCUM (https://ucum.org) conventions.

## Usage
Start the service from command line (works directly from inside Maven):
```
$ mvn clean compile exec:java
```

The default port of the service is set to 8384 and listens on localhost (127.0.0.1) only.
Use the following url to convert a {unit} into ucum format:
```
http://localhost:8384/v1/api/quantity/{unit}
```

To start it from a different host/port, pass `-Dhost=...` and `-Dport=...`.
If you want to make it available under a different context path (default is `/`),
you can add something like `-DcontextPath=/pucum` to startup parameters, so the
query URL gets:
```
http://localhost:8384/pucum/v1/api/quantity/{unit}
```

An example of response returned by the service for the unit 'mg/ml'
```
{
  "input": "mg/ml",
  "status": "201_QUANTITY_FOUND",
  "status_msg": "The quantities (UCUM and/or QUDT) are available.",
  "ucum": "mg/ml",
  "fullname": "(milligram) / (milliliter)",
  "canonicalunit": "g.m-3",
  "dimension": "M.L-3",
  "qudt_quantity": [
  {
    "name": "Density",
    "id": 43792
  }
  ],
"ucum_quantity": null,
"verbosecanonicalunit": "mass &middot; length<sup>-3</sup>"
}
```
