Template Directives
===================

The features templating plug-in works by applying a template file as a mapping level over the stream of features received by a store, allowing a fine grained control over the final output of a getFeature request.
The first part of the plug-in documentation will go through the template syntax. The second one will show how to configure the template to apply it to a vector layer. The third part shows the backwards mapping functionality.

This part of the documentation is an introduction explaining the different template directives that can be used to customize a GetFeature output. Examples will be provided for both Simple Features and Complex Features.
There are two kind of main template types:

* JSON based templates (GeoJSON and JSON-LD).
* XML based templates (at the time of writing only GML).

The examples will be provided mainly for GeoJSON and GML. Anyway the syntax defined for GeoJSON output, unless otherwise specified, will works as well for JSON-LD templates.


Template directive summary
--------------------------

The following constitues a summary of the all the template directives and is meant to have a quick reference. Each directive is explained in details in the sections below.

.. list-table::
   :widths: 10 10 30 60

   * - **Directive**
     - **Template type**
     - **Usage**
     - **Description**
   * - ${property} and $${cql}
     - JSON based templates
     - property interpolation and cql evaluation
     - specify them as an attribute value (:code:`"json_attribute":"${property}"` or :code:`"json_attribute":"$${cql}"`)
   * - ${property} and $${cql}
     - XML based templates
     - property interpolation and cql evaluation
     - specify them either as an element value (:code:`<element>$${cql}</element>`) or as an xml attribute value (:code:`<element attribute:"${property}"/>`)
   * - $source
     - JSON based templates
     - setting the evaluation context for property interpolation and cql evaluation in child attributes.
     - specify it as the first nested object in arrays (:code:`{"$source":"property"}`) or as an attribute in objects (:code:`"$source":"property"`)
   * - gft:source
     - XML based templates
     - setting the evaluation context for property interpolation and cql evaluation in child elements.
     - specify it as an xml attribute (:code:`<element gft:source:"property">`)
   * - $filter
     - JSON based templates
     - filter the array, object, attribute to which is applied based on the defined condition
     - specify it inside the first nested object in arrays (:code:`{"$filter":"condition"}`) or as an attribute in objects (:code:`"$filter":"condition"`) or in an attribute next to the attribute value separated by a :code:`,` (:code:`"attribute":"$filter{condition}, ${property}"`)
   * - gft:filter
     - XML based templates
     - filter the element to which is applied based on the defined condition
     - specify it as an XML attribute on the element to be filtered (:code:`<element gft:filter:"condition">`)
   * - gft:Template
     - XML based templates
     - Marks the beginning of an XML template.
     - It has to be the root element of an XML template (:code:`<gft:Template> Template content</gft:Template>`)
   * - $options
     - JSON based templates
     - defines options to customize the output outside of a feature scope
     - specify it at the top of the JSON template as a JSON object (:code:`"$options":{"flat_output":true, "separator":"."}`). GeoJSON options: :code:`flat_output`,:code:`separator`. JSON-LD option: :code:`@context`
   * - gft:Options
     - XML based templates
     - defines options to customize the output outside of a feature scope
     - specify it as an element at the beggining of the xml document after the :code:`<gft:Template>` one (:code:`<gft:Options></gft:Options>`). GML options: :code:`<gtf:Namespaces>`,:code:`<gtf:SchemaLocation>`
   * - $include, $includeFlat
     - JSON based templates
     - allows to include a template into another
     - specify the :code:`$include` option as an attribute value (:code:`"attribute":"$include{subProperty.json}"`) and the :code:`$includeFlat` as an attribute name with the included template path as a value (:code:`"$includeFlat":"included.json"`)
   * - $include, gft:includeFlat
     - XML based templates
     - allows to include a template into another
     - specify the :code:`$include` option as an element value (:code:`<element>$include{included.xml}</element>`) and the :code:`gft:includeFlat` as an element having the included tempalte as text content (:code:`<gft:includeFlat>included.xml</gft:includeFlat>`)
   * - $merge
     - JSON based templates
     - allows a template to extend another template
     - specify the :code:`$merge` directive as an attribute name containing the path to the extended template (:code: `"$merged":"base_template.json"`)

A step by step introduction to features-templating syntax
---------------------------------------------------------
This introduction is meant to illustrates the different directives that can be used in a template. For clarity we will go before through a Simple Feature example and then through a Complex Feature one. However all the directives that will be showed are available for both Simple and Complex Features. 
We will mostly use GeoJSON and GML examples. For JSON-LD output format the rule to define a template are the same of the GeoJSON template with two exceptions:
* A @context needs to be specified (see the options section below).
* The standard mandates that attributes' values are all strings.



${property} and $${cql} directive (Simple Feature example)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Assume that we want to change the default geojson output of the :code:`topp:states` layer. A single feature in the default output is like the following:

.. code-block:: json

  {
   "type": "Feature",
    "id": "states.1",
    "geometry": {},
    "geometry_name": "the_geom",
    "properties": {
    "STATE_NAME": "Illinois",
    "STATE_FIPS": "17",
    "SUB_REGION": "E N Cen",
    "STATE_ABBR": "IL",
    "LAND_KM": 143986.61,
    "WATER_KM": 1993.335,
    "PERSONS": 11430602,
    "FAMILIES": 2924880,
    "HOUSHOLD": 4202240,
    "MALE": 5552233,
    "FEMALE": 5878369,
    "WORKERS": 4199206,
    "DRVALONE": 3741715,
    "CARPOOL": 652603,
    "PUBTRANS": 538071,
    "EMPLOYED": 5417967,
    "UNEMPLOY": 385040,
    "SERVICE": 1360159,
    "MANUAL": 828906,
    "P_MALE": 0.486,
    "P_FEMALE": 0.514,
    "SAMP_POP": 1747776
    }
  }

In particular we want to include in the final output only certain properties (e.g. the geometry, the state name, the code, values about population, male, female and workers). We want also to change some attribute names and to have them lower cased. Finally we want to have a string field having a wkt representation of the geometry. The desired output is like the following:

.. code-block:: json

 {
   "type":"Feature",
   "id":"states.1",
   "geometry":{
      "type":"MultiPolygon",
      "coordinates":"[....]"   
   },
   "properties":{
      "name":"Illinois",
      "region":"E N Cen",
      "code":"IL",
      "population_data":{
         "population":114306027,
         "males":5552233.0,
         "females":5878369.0,
         "active_population":4199206.0
      },
      "wkt_geom":"MULTIPOLYGON (((37.51099000000001 -88.071564, [...])))"
   }
 }

A template like this will allows us to produce the above output:

.. code-block:: json

  {
  "type": "Feature",
  "id": "${@id}",
  "geometry": "${the_geom}",
  "properties": {
      "name": "${STATE_NAME}",
      "region": "${SUB_REGION}",
      "code": "${STATE_ABBR}",
      "population_data":{
          "population": "${PERSONS}",
          "males": "${MALE}",
          "females": "${FEMALE}",
          "active_population": "${WORKERS}"
      },
      "wkt_geom":"$${toWKT(the_geom)}"
  }
 }



As it is possible to see the new output has the attribute names defined in the template. Moreover the :code:`population` related attributes have been placed inside a nested json object. Finally we added a wkt_geom attribute with the WKT geometry representation.

The same template mechanism can be applied to a GML output format. This is an example GML template, again for the :code:`topp:states` layer

.. code-block:: xml

  <gft:Template>
   <topp:states gml:id="${@id}">
     <topp:name code="${STATE_ABBR}">${STATE_NAME}</topp:name>
     <topp:region>${SUB_REGION}</topp:region>
     <topp:population>${PERSONS}</topp:population>
     <topp:males>${MALE}</topp:males>
     <topp:females>${FEMALE}</topp:females>
     <topp:active_population>${WORKERS}</topp:active_population>
     <topp:wkt_geom>$${toWKT(the_geom)}</topp:wkt_geom>
   </topp:states>
 </gft:Template>

And this is how a feature will appear:

.. code-block:: xml

   <topp:states gml:id="states.10">
      <topp:name code="MO">Missouri</topp:name>
      <topp:region>W N Cen</topp:region>
      <topp:population>5117073.0</topp:population>
      <topp:males>2464315.0</topp:males>
      <topp:females>2652758.0</topp:females>
      <topp:active_population>1861192.0</topp:active_population>
      <topp:wkt_geom>MULTIPOLYGON (([....])))</topp:wkt_geom>
    </topp:states>

As it is possible to see we are encoding the geometry only as a wkt, moreover the STATE_ATTR value is now present as an xml attribute of the element :code:`topp:states`. Finally elements that were not defined in the template did not showed up.

Looking at these examples it is possible to see that we have used few directives to customize the output:

* Property interpolation can be invoked using the directive :code:`${property_name}`.
* In case complex operation are needed a CQL expression can be used throught a :code:`$${cql}` syntax (all CQL functions are supported).
* Simple text values are reproduced in the final output as they are.
* Finally the gml template needs the actual template content to be wrapped into a :code:`gft:Template` element. The :code:`gft` doesn't needs to be bound to a namespaces. It is used just as marker of features-templating related element and will not be present in the final output.

Source and filter (Complex Feature example)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Lets assume now that we have a configured AppSchema layer and we want to customize the complex features output.
We will use as an example the Meteo Stations use case. For a description of the use case check the documentation at :ref:`community_smart_data_loader`.

The default GeoJSON output format produces features like the following:

.. code-block:: json

 {
   "type":"Feature",
   "id":"MeteoStationsFeature.7",
   "geometry":{
      
   },
   "properties":{
      "@featureType":"MeteoStations",
      "id":7,
      "code":"BOL",
      "common_name":"Bologna",
      "meteoObservations":[
         {
            "id":3,
            "time":"2016-12-19T11:28:31Z",
            "value":35,
            "meteoParameters":[
               {
                  "id":1,
                  "param_name":"temperature",
                  "param_unit":"C"
               }
            ]
         },
         {
            "id":4,
            "time":"2016-12-19T11:28:55Z",
            "value":25,
            "meteoParameters":[
               {
                  "id":1,
                  "param_name":"temperature",
                  "param_unit":"C"
               }
            ]
         },
         {
            "id":5,
            "time":"2016-12-19T11:29:24Z",
            "value":80,
            "meteoParameters":[
               {
                  "id":2,
                  "param_name":"wind speed",
                  "param_unit":"Km/h"
               }
            ]
         },
         {
            "id":6,
            "time":"2016-12-19T11:30:26Z",
            "value":1019,
            "meteoParameters":[
               {
                  "id":3,
                  "param_name":"pressure",
                  "param_unit":"hPa"
               }
            ]
         },
         {
            "id":7,
            "time":"2016-12-19T11:30:51Z",
            "value":1015,
            "meteoParameters":[
               {
                  "id":3,
                  "param_name":"pressure",
                  "param_unit":"hPa"
               }
            ]
         }
      ]
   }
 }


From the above JSON we can see a data structure where:

* We have a Station object with a nested array of Observations.
* Each Observation has a an array of parameter that describe the type of Observation.

Now we want to produce a different output where instead of having a generic array of observation nested in to the root object, we have one array for each type of parameter e.g. Temperatures, Pressures and Winds_speed observations. In other words instead of having the Observation type defined inside a nested Parameter object we want to have that information directly in the attribute name.
The pursued output looks like the following:

.. code-block:: json

  {
   "type":"FeatureCollection",
   "features":[
      {
         "Identifier":"MeteoStationsFeature.7",
         "geometry":{
            "type":"Point",
            "coordinates":[
               44.5,
               11.34
            ]
         },
         "properties":{
            "Name":"Bologna",
            "Code":"STATION-BOL",
            "Location":"POINT (44.5 11.34)",
            "Temperatures":[
               {
                  "Timestamp":"2016-12-19T11:28:31.000+00:00",
                  "Value":35.0
               },
               {
                  "Timestamp":"2016-12-19T11:28:55.000+00:00",
                  "Value":25.0
               }
            ],
            "Pressures":[
               {
                  "Timestamp":"2016-12-19T11:30:26.000+00:00",
                  "Value":1019.0
               },
               {
                  "Timestamp":"2016-12-19T11:30:51.000+00:00",
                  "Value":1015.0
               }
            ],
            "Winds_speed":[
               {
                  "Timestamp":"2016-12-19T11:29:24.000+00:00",
                  "Value":80.0
               }
            ]
         }
      }
   ],
   "totalFeatures":3,
   "numberMatched":3,
   "numberReturned":1,
   "timeStamp":"2021-07-13T14:00:19.457Z",
   "crs":{
      "type":"name",
      "properties":{
         "name":"urn:ogc:def:crs:EPSG::4326"
      }
   }
 }


A template like this will allow to produce such an output:

.. code-block:: json

   {
        "$source":"st:MeteoStationsFeature",
        "Identifier":"${@id}",
        "geometry":"${st:position}",
        "properties":{
        "Name":"${st:common_name}",
        "Code":"$${strConcat('STATION-', xpath('st:code'))}",
        "Location":"$${toWKT(xpath('st:position'))}",
        "Temperatures":[
          {
            "$source":"st:meteoObservations/st:MeteoObservationsFeature",
            "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'temperature'"
          },
          {
            "Timestamp": "${st:time}",
            "Value": "${st:value}"
          }
        ],
        "Pressures":[
          {
            "$source":"st:meteoObservations/st:MeteoObservationsFeature",
            "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'pressure'"
          },
          {
            "Timestamp": "${st:time}",
            "Value": "${st:value}"
          }
        ],
        "Winds_speed":[
          {
            "$source":"st:meteoObservations/st:MeteoObservationsFeature",
            "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'wind speed'"
          },
          {
            "Timestamp": "${st:time}",
            "Value": "${st:value}"
          }
        ]
      }
     }


In addition to the :code:`${property}` and :code:`$${cql}` directives we saw before there are two more:

* We see the usage of the :code:`xpath('xpath')` function to reference property. When dealing with Complex Features it must be used when referencing properties inside a :code:`$filter` or a :code:`$${cql}` directives.
* :code:`$source` which is meant to provide the context against which evaluated nested element properties and xpaths. In this case the :code:`"$source":"st:meteoObservations/st:MeteoObservationsFeature"` provides the context for the nested attributes angainst which the directives will be evaluated. When defining a :code:`$source` for a JSON array it should be provided in a JSONObject separated from the JSON Object mapping the nested feature attributes as in the example above. When defining the :code:`$source` for a JSONObject it can be simply added as an object attribute (see below examples).
* When using :code:`${property}` directive or an :code:`xpath('xpath')` function it is possible to reference a property bounded to an upper :code:`$source` using a ``../`` notation eg. ``${../previousContextValue}``.
* :code:`$filter` provides the possibility to filter the value that will be included in the element to which is applied, in this case a json array. For instance the filter :code:`$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'wind speed'` in the :code:`Winds_speed` array allows to filter the element that will be included in this array according to the :code:`param_name value`.

.. warning:: the :code:`xpath('some xpath)` cql function is meant to be used in the scope of this plugin. For general usage please refers to the :geotools:`property function <library/main/function_list.html#property-propertyname-returns-propertyvalue>`.

Follows a list of JSON template bits showing  :code:`filters` definition in context different from a JSON array, as well as :code:`$source` definition for a JSONObject.

* Object (encode the JSON object only if the st:value is greater than 75.3).

.. code-block:: json

 {
   "Observation":
         {
           "$source":"st:MeteoObservationsFeature",
           "$filter":"st:value > 75.3 ",
           "Timestamp":"${st:time}",
           "Value":"${st:value}"
        }
 }



* Attribute (encode the Timestamp attribute only if the st:value is greater than 75.3).

.. code-block:: json

  {
  "Observation":
         {
           "$source":"st:MeteoObservationsFeature",
           "Timestamp":"$filter{st:value > 75.3}, ${st:time}",
           "Value":"${st:value}"
        }
  }


* Static attribute  (encode the Static_value attribute only if the st:value is greater than 75.3).

.. code-block:: json

   {
  "Observation":
         {
           "$source":"st:MeteoObservationsFeature",
           "Timestamp":"${st:time}",
           "Static_value":"$filter{st:value > 75.3}, this Observation has a value > 75.3",
           "Value":"${st:value}"
        }
  }


As it is possible to see from the previous example in the array and object cases the filter sintax expected a :code:`"$filter"` key followed by an attribute with the filter to evaluate. In the attribute case, instead, the filter is being specified inside the value as :code:`"$filter{...}"`, followed by  the cql expression, or by the static content, with a comma separating the two.


:code:`filter` and :code:`source` are available as well in GML template. As an example, assume that we want to obtain the correspective GML output of the GeoJSON output above e.g.:

.. code-block:: xml

   <?xml version="1.0" encoding="UTF-8"?>
   <wfs:FeatureCollection xmlns:st="http://www.stations.org/1.0" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:wfs="http://www.opengis.net/wfs/2.0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml/3.2" numberMatched="3" numberReturned="0" timeStamp="2021-07-13T15:09:28.620Z">
  <wfs:member>
    <st:MeteoStations gml:id="MeteoStationsFeature.7">
      <st:code>Station_BOL</st:code>
      <st:name>Bologna</st:name>
      <st:geometry>
        <gml:Point srsName="urn:ogc:def:crs:EPSG::4326" srsDimension="2" gml:id="smdl-stations.1.geom">
          <gml:pos>11.34 44.5</gml:pos>
        </gml:Point>
      </st:geometry>
      <st:temperature>
        <st:temperature>
          <st:Temperature>
            <st:time>2016-12-19T11:28:31.000Z</st:time>
            <st:value>35.0</st:value>
          </st:Temperature>
        </st:temperature>
        <st:temperature>
          <st:Temperature>
            <st:time>2016-12-19T11:28:55.000Z</st:time>
            <st:value>25.0</st:value>
          </st:Temperature>
        </st:temperature>
      </st:temperature>
      <st:pressure>
        <st:pressure>
          <st:Pressure>
            <st:time>2016-12-19T11:30:26.000Z</st:time>
            <st:value>1019.0</st:value>
          </st:Pressure>
        </st:pressure>
        <st:pressure>
          <st:Pressure>
            <st:time>2016-12-19T11:30:51.000Z</st:time>
            <st:value>1015.0</st:value>
          </st:Pressure>
        </st:pressure>
      </st:pressure>
      <st:wind_speed>
        <st:wind_speed>
          <st:Wind_speed>
            <st:time>2016-12-19T11:29:24.000Z</st:time>
            <st:value>80.0</st:value>
          </st:Wind_speed>
        </st:wind_speed>
      </st:wind_speed>
    </st:MeteoStations>
  </wfs:member>
 </wfs:FeatureCollection>


The following GML template will produce the above output:

.. code-block:: xml

  <gft:Template>
  <gft:Options>
    <gft:Namespaces xmlns:st="http://www.stations.org/1.0"/>
  </gft:Options>
  <st:MeteoStations gml:id="${@id}">
  <st:code>$${strConcat('Station_',st:code)}</st:code>
  <st:name>${st:common_name}</st:name>
  <st:geometry>${st:position}</st:geometry>
  <st:temperature gft:isCollection="true" gft:source="st:meteoObservations/st:MeteoObservationsFeature" gft:filter="xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'temperature'">
  <st:Temperature>
    <st:time>${st:time}</st:time>
    <st:value>${st:value}</st:value>
  </st:Temperature>
  </st:temperature>
  <st:pressure gft:isCollection="true" gft:source="st:meteoObservations/st:MeteoObservationsFeature"  gft:filter="xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'pressure'">
  <st:Pressure>
    <st:time>${st:time}</st:time>
    <st:value>${st:value}</st:value>
  </st:Pressure>
  </st:pressure>
  <st:wind_speed gft:isCollection="true" gft:source="st:meteoObservations/st:MeteoObservationsFeature"  gft:filter="xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'wind speed'">
  <st:Wind_speed>
    <st:time>${st:time}</st:time>
    <st:value>${st:value}</st:value>
  </st:Wind_speed>
  </st:wind_speed>
  </st:MeteoStations>
 </gft:Template>


In the GML case :code:`filter` and :code:`source` directives are defined in a slightly different manner from the JSON usecase.

* The filter needs to be defined as an attribute :code:`gft:filter` in the element that we want to filter.
* The source needs to be defined as an attribute :code:`gft:source` in the element that will set the source for its child elements.
* The attribute :code:`gft:isCollection="true"` define a directive meant to be used in GML templates to mark collection elements: this directive is needed since xml doesn't have the array concept and the template mechanism needs to be informed if an element should be repeated because it represent a collection element. 
* Finally we see one more directive defined through the element :code:`gft:Options`. We will go throught this in the next section.


Template Options
^^^^^^^^^^^^^^^^

The directives that we have seen so far allow to control the output in the scope of a Feature element. 
The :code:`options` directive, instead, allows to customize the output for piece of the output outside the Feature scope or to define general modifications to the overall output. The available options vary according to the output format.

GeoJSON
"""""""
In the context of a GeoJSON template are available two options: :code:`flat_output` and :code:`separator`. These options are meant to provide a GeoJSON output encoded following INSPIRE rule for `alternative feature GeoJSON encoding <https://github.com/INSPIRE-MIF/2017.2/blob/master/GeoJSON/ads/simple-addresses.md>`_ (`see also <https://github.com/INSPIRE-MIF/2017.2/blob/master/GeoJSON/efs/simple-environmental-monitoring-facilities.md>`_).
To use the functionality an :code:`"$options"` JSON object can be added on top of a JSON template, like in the following example:

.. code-block:: json

   {
        "$options":{
          "flat_output":true,
          "separator": "."
        },
        "$source":"st:MeteoStationsFeature",
        "Identifier":"${@id}",
        "geometry":"${st:position}",
        "properties":{
        "Name":"${st:common_name}",
        "Code":"$${strConcat('STATION-', xpath('st:code'))}",
        "Location":"$${toWKT(xpath('st:position'))}",
        "Temperatures":[
          {
            "$source":"st:meteoObservations/st:MeteoObservationsFeature",
            "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'temperature'"
          },
          {
            "Timestamp": "${st:time}",
            "Value": "${st:value}"
          }
        ],
        "Pressures":[
          {
            "$source":"st:meteoObservations/st:MeteoObservationsFeature",
            "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'pressure'"
          },
          {
            "Timestamp": "${st:time}",
            "Value": "${st:value}"
          }
        ],
        "Winds_speed":[
          {
            "$source":"st:meteoObservations/st:MeteoObservationsFeature",
            "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'wind speed'"
          },
          {
            "Timestamp": "${st:time}",
            "Value": "${st:value}"
          }
        ]
      }
     }

The :code:`flat_output` will act in the following way:

 * The encoding of nested arrays and objects will be skipped, by encoding only their attributes.
 * Objects' attribute names will be concatenated with the ones of their json attributes.
 * Arrays' attribute names will be concatenated as well with the one of the json attributes of their inner object. In addition an index value will be added after the array's attribute name for each nested object.
 * The :code:`separator` specifies the separator of the attributes' names. Default is :code:`_`.
 * The final output will have a flat list of attributes with names produced by the concatenation, like the following.


JSON-LD
""""""""
A JSON-LD template can be defined as a GeoJSON template since it is a JSON based output as well. However it needs to have a :code:`@context` attribute, object or array at the beginning of it in orther to conform to the standard.
To accomplish this requirement it is possible to specify the :code:`@context` as an :code:`option` in the template, like in the following one:

.. code-block:: json

  {
   "$options":{
      "@context":[
         "https://opengeospatial.github.io/ELFIE/contexts/elfie-2/elf-index.jsonld",
         "https://opengeospatial.github.io/ELFIE/contexts/elfie-2/gwml2.jsonld",
         {
            "gsp":"http://www.opengis.net/ont/geosparql#",
            "sf":"http://www.opengis.net/ont/sf#",
            "schema":"https://schema.org/",
            "st":"http://www.stations.org/1.0",
            "wkt":"gsp:asWKT",
            "Feature":"gsp:Feature",
            "geometry":"gsp:hasGeometry",
            "point":"sf:point",
            "features":{
               "@container":"@set",
               "@id":"schema:hasPart"
            }
         }
      ]
   },
   "$source":"st:MeteoStationsFeature",
   "Identifier":"${@id}",
   "Name":"${st:common_name}",
   "Code":"$${strConcat('STATION-', xpath('st:code'))}",
   "Location":"$${toWKT(st:position)}",
   "Temperatures":[
      {
         "$source":"st:meteoObservations/st:MeteoObservationsFeature",
         "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'temperature' AND 'yes' = env('showTemperatures','yes')"
      },
      {
         "Timestamp":"${st:time}",
         "Value":"${st:value}"
      }
   ],
   "Pressures":[
      {
         "$source":"st:meteoObservations/st:MeteoObservationsFeature",
         "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'pressure' AND 'yes' = env('showPressures','yes')"
      },
      {
         "Timestamp":"${st:time}",
         "Value":"${st:value}"
      }
   ],
   "Winds speed":[
      {
         "$source":"st:meteoObservations/st:MeteoObservationsFeature",
         "$filter":"xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'wind speed' AND 'yes' = env('showWinds','yes')"
      },
      {
         "Timestamp":"${st:time}",
         "Value":"${st:value}"
      }
   ]
 }

The :code:`@context` will show up at the beginning of the JSON-LD output:

.. code-block:: json

 {
   "@context":[
      "https://opengeospatial.github.io/ELFIE/contexts/elfie-2/elf-index.jsonld",
      "https://opengeospatial.github.io/ELFIE/contexts/elfie-2/gwml2.jsonld",
      {
         "gsp":"http://www.opengis.net/ont/geosparql#",
         "sf":"http://www.opengis.net/ont/sf#",
         "schema":"https://schema.org/",
         "st":"http://www.stations.org/1.0",
         "wkt":"gsp:asWKT",
         "Feature":"gsp:Feature",
         "geometry":"gsp:hasGeometry",
         "point":"sf:point",
         "features":{
            "@container":"@set",
            "@id":"schema:hasPart"
         }
      }
   ],
   "type":"FeatureCollection",
   "features":[
      {
         "Identifier":"MeteoStationsFeature.7",
         "Name":"Bologna",
         "Code":"STATION-BOL",
         "Location":"POINT (44.5 11.34)",
         "Temperatures":[
            {
               "Timestamp":"2016-12-19T11:28:31.000+00:00",
               "Value":"35.0"
            },
            {
               "Timestamp":"2016-12-19T11:28:55.000+00:00",
               "Value":"25.0"
            }
         ],
         "Pressures":[
            {
               "Timestamp":"2016-12-19T11:30:26.000+00:00",
               "Value":"1019.0"
            },
            {
               "Timestamp":"2016-12-19T11:30:51.000+00:00",
               "Value":"1015.0"
            }
         ],
         "Winds speed":[
            {
               "Timestamp":"2016-12-19T11:29:24.000+00:00",
               "Value":"80.0"
            }
         ]
      }
   ]
 }


GML
"""

GML output has two :code:`options`: Namespaces and SchemaLocation, that define the namspaces and the SchemaLocation attribute that will be included in the FeatureCollection element in the resulting output. These options needs to be specified inside a :code:`gft:Options` element at the beggining of the template right after the :code:`gft:Template` element, e.g.

.. code-block:: xml

  <gft:Template>
   <gft:Options>
     <gft:Namespaces xmlns:st="http://www.stations.org/1.0"/>
     <gft:SchemaLocation xsi:schemaLocation="http://www.stations.org/1.0 http://www.stations.org/stations/1.0/xsd/stations.xsd"/>
   </gft:Options>
   <st:MeteoStations gml:id="${@id}">
  <st:code>$${strConcat('Station_',st:code)}</st:code>
  <st:name>${st:common_name}</st:name>
  <st:geometry>${st:position}</st:geometry>
  <st:temperature gft:isCollection="true" gft:source="st:meteoObservations/st:MeteoObservationsFeature" gft:filter="xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'temperature'">
  <st:Temperature>
    <st:time>${st:time}</st:time>
    <st:value>${st:value}</st:value>
  </st:Temperature>
  </st:temperature>
  <st:pressure gft:isCollection="true" gft:source="st:meteoObservations/st:MeteoObservationsFeature"  gft:filter="xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'pressure'">
  <st:Pressure>
    <st:time>${st:time}</st:time>
    <st:value>${st:value}</st:value>
  </st:Pressure>
  </st:pressure>
  <st:wind_speed gft:isCollection="true" gft:source="st:meteoObservations/st:MeteoObservationsFeature"  gft:filter="xpath('st:meteoParameters/st:MeteoParametersFeature/st:param_name') = 'wind speed'">
  <st:Wind_speed>
    <st:time>${st:time}</st:time>
    <st:value>${st:value}</st:value>
  </st:Wind_speed>
  </st:wind_speed>
  </st:MeteoStations>
  </gft:Template>




Including other templates
-------------------------

While developing a group of templates, it's possible to notice sections that repeat across 
different template instances. Template inclusion allows to share the common parts, extracting them
in a re-usable building block.

Inclusion can be performed using two directives:

* :code:`include` allows to include a separate template as is.
* :code:`includeFlat` allows to include a separate template, stripping the top-most container. 

As for other directives the syntax varies slightly between JSON based template and XML based ones.

The two directives need to specify a path to the template to be included.
Template names can be plain, as in this example, refer to sub-directories, or be absolute. 
Examples of valid template references are:

* ``subProperty.json``
* ``./subProperty.json``
* ``./blocks/aBlock.json``
* ``/templates/test/aBlock.json``

However it's currently not possible to climb up the directory hierarchy using relative references, 
so a reference like ``../myParentBlock.json`` will be rejected.

JSON based templates (GeoJSON, JSON-LD)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In this context the two directives can be defined as:

* :code:`$include`.
* :code:`$includeFlat`. 

Regarding the :code:`$includeFlat` option is worth mentioning that in a JSON context:
* If a JSON object is included, then its properties are directly included in-place, which makes sense only within another object. 
* If instead a JSON array is included, then its values are directly included in-place, which makes sense only within another array.

The following JSON snippet shows the four possible syntax options for template inclusion:

.. code-block:: json
   :linenos: 

    {
       "aProperty": "$include{subProperty.json}", 
       "$includeFlat": "propsInAnObject.json", 
       "anArray" : [
          "$include{arrayElement.json}", 
          "$includeFlat{subArray.json}" 
       ]
    }

Notes:

1) The ``subProperty.json`` template (line 2) can be both an object or an array, it will be used as the new value of ``aProperty``
2) The ``propsInAnObject.json`` template (line 3) is required to be a JSON object, its properties will be 
   directly included in-place where the ``$includeFlat`` directive is
3) The ``arrayElement.json`` template (line 5) can be both an object or an array, the value will be replaced
   directly as the new element in ``anArray``. This allows creation of a JSON object as the array
   element, or the creation of a nested array.
4) The ``subArray.json`` template (line 6) must be an array itself, the container array will be stripped and
   its values directly integrated inside ``anArray``.


XML based templates (GML)
^^^^^^^^^^^^^^^^^^^^^^^^^^

In an XML context the two directives needs to be defined in the following way:

* :code:`<gft:includeFlat>path/to/included.xml</gft:includeFlat>`.
* :code:`<gsml:specification gft:source="gsml:specification">$include{includedTemplate.xml}</gsml:specification>`.

In the first case the included template will replace the :code:`<gft:includeFlat>` element. In the second one the included template will be placed inside the :code:`<gsml:specification>` element.

Extending other templates via merge (JSON based templates only)
---------------------------------------------------------------

Templates inclusion, described above, allows to import a block into another template, as is.
The ``$merge`` directive instead allows to get an object and use it as a base, that will be
overridden by the properties of the object it is merged into.

For example, let's assume this is a base JSON template:

.. code-block:: json

      {
        "a": 10,
        "b": "${attribute1}",
        "c": "${attribute2}",
        "array": [1, 2, 3]
      }

and this is a template extending it:

.. code-block:: json

      {
        "$merge": "base.json",
        "a": {
          "a1": 1,
          "a2": 2
        },
        "b": null,
        "d": "${customAttribute}"
      }

The template actually being processed would look as follows:

.. code-block:: json

      {
        "a": {
          "a1": 1,
          "a2": 2
        },
        "c": "${attribute2}",
        "array": [1, 2, 3]
        "d": "${customAttribute}"
      }

The general rules for object merging are:

* Overridden simple properties are replaced.
* Properties set to null are removed.
* Nested objects available in both trees are drilled down, being recursively merged. 
* Arrays are replaced as-is, with no merging. The eventual top level ``features`` array is the only
  exception to this rule.
* While order of the keys is not important in JSON, the merge is processed so that the base 
  property names are included first in the merged result, and the new ones included in the override 
  are added after them.

The ``$merge`` directive can be used in any object, making it the root for the merge operation.
This could be used as an alternative to inclusion when local customizations are needed.


Environment parametrization
---------------------------

A template configuration can also be manipulated on the fly, replacing existing attributes, attributes' names and sources using the :code:`env` parameter. 
To achieve this the attribute name, the attribute, or the source should be replaced by the env function in the following way :code:`$${env('nameOfTheEnvParameter','defaultValue')}`. 
If in the request it is specified an env query parameter :code:`env='nameOfTheEnvParameter':'newValue'`, the default value will be replaced in the final output with the one specified in the request.

The functionality allows also to manipulate dynamically filters and expression. For example it is possible to change Filter arguments: :code:`"$filter":"xpath('gsml:name') = env('nameOfTheEnvParameter','defaultValue')`.

Xpaths can be manipulated as well to be totally or partially replaced: :code:`$${xpath(env('xpath','gsml:ControlledConcept/gsml:name')}` or :code:`$${xpath(strConcat('env('gsml:ControlledConcept',xpath','/gsml:name')))}`.

JSON based properties
---------------------

Certain databases have native support for JSON fields. For example, PostgreSQL has both a JSON
and a JSONB type. The JSON templating machinery can recognize these fields and export them
as JSON blocks, for direct substitution in the output.

It is also possible to pick a JSON attribute and use the ``jsonPointer`` function to extract either
a property or a whole JSON subtree from it. See the `JSON Pointer RFC <https://datatracker.ietf.org/doc/html/rfc6901>`_ 
for more details about valid expressions.

Here is an example of using JSON properties:

.. code-block:: json
   :linenos:

   {
      "assets": "${assets}",
      "links": [
        "$${jsonPointer(others, '/fullLink')}",
        {
          "href": "$${jsonPointer(others, '/otherLink/href')}",
          "rel": "metadata",
          "title": "$${jsonPointer(others, '/otherLink/title')}",
          "type": "text/xml"
        }
      ]
   }

Some references:

- ``Line 1`` uses ``assets``, a property that can contain a JSON tree of any shape, which will be 
  expanded in place.
- ``Line 4`` inserts a full JSON object in the array. The object is a sub-tree of the ``others`` property,
  which is a complex JSON document with several extra properties (could be a generic containers for
  properties not fitting the fixed database schema).
- ``Line 6`` and ``Line 8`` extract from the ``others`` property specific string values.


Array based properties (JSON based templates only)
--------------------------------------------------

Along JSON properties, it's not rare to find support for array based attributes in modern databases.
E.g. ``varchar[]`` is a attributes containing an array of strings.

The array properties can be used as-is, and they will be expanded into a JSON array.
Let's assume the ``keywords`` database column contains a list of strings, then the following template:

.. code-block:: json
   :linenos:

   {
      "keywords": "${keywords}"
   }


May expand into:

.. code-block:: json
   :linenos:

   {
      "keywords": ["features", "templating"]
   }

It is also possible to use an array as the source of iteration, referencing the current
array item using the ``${.}`` XPath. For example:

.. code-block:: json
   :linenos:

   {
      "metadata": [
         {
            "$source": "keywords"
         },
         {
            "type": "keyword",
            "value": "${.}"
         }
      ]
   }

The above may expand into:

.. code-block:: json
   :linenos:

   {
      "metadata": [
         {
            "type": "keyword",
            "value": "features"
         },
         {
            "type": "keyword",
            "value": "templating"
         }
      ]
   }

In case a specific item of an array needs to be retrieved, the ``item`` function can be used,
for example, the following template extracts the second item in an array (would fail if not
present):

.. code-block:: json
   :linenos:

   {
      "second": "$${item(keywords, 1)}"
   }


There is currently no explicit support for array based columns in GML templates.


Simplfied Property Access
--------------------------

The features-templating plug-in provides the possibility to directly reference domain name when dealing with Complex Features and using property interpolation in a template.
As an example lets use again the meteo stations use case. This is the ER diagram of the Database table involved.

.. figure:: images/meteos-stations-er-diagram.png

The following is a GeoJSON template that directly reference table names and column name, instead of referencing the target Xpath in the AppSchema mappings.

.. code-block:: json
 
 {
   "$source":"meteo_stations",
   "Identifier":"${id}",
   "Name":"${common_name}",
   "Code":"$${strConcat('STATION-', xpath('code'))}",
   "Location":"$${toWKT(position)}",
   "Temperatures":[
      {
         "$source":"meteo_observations",
         "$filter":"propertyPath('->meteo_parameters.param_name') = 'temperature' AND 'yes' = env('showTemperatures','yes')"
      },
      {
         "Timestamp":"${time}",
         "Value":"${value}"
      }
   ],
   "Pressures":[
      {
         "$source":"meteo_observations",
         "$filter":"propertyPath('->meteo_parameters.param_name') = 'pressure' AND 'yes' = env('showPressures','yes')"
      },
      {
         "Timestamp":"${time}",
         "Value":"${value}"
      }
   ],
   "Winds speed":[
      {
         "$source":"meteo_observations",
         "$filter":"propertyPath('->meteo_parameters.param_name') = 'wind speed' AND 'yes' = env('showWinds','yes')"
      },
      {
         "Timestamp":"${time}",
         "Value":"${value}"
      }
   ]
 }

As it is possible to see this template has some differences comparing to the one seen above:

* Property interpolation  (``${property}``) and cql evaluation (``$${cql}``) directives are referencing the column name of the attribute that we want to include in the final output. The names match the ones of the columns and no namepsaces prefix is being used.
* Inside the $${cql} directive instead of using an ``xpath`` function we are using the ``propertyPath`` function. It must be used when we want reference domain names inside a ``$${cql}`` directive. Paths in this case are no more separated by a ``/`` but by a ``.`` dot.
* The ``$source`` directive references the table names.
* When a ``column/property`` in a ``table/source`` is referenced from the context of the upper ``table/source``, as in all the filters in the template, the table name needs to be prefixed with a ``->`` symbol, and column name can come next separated by a ``.`` dot.

.. warning:: the :code:`propertyPath('propertyPath')` cql function is meant to be used only in the scope of this plugin. It is not currently possible to reference domain property outside the context of a template file.

This functionality is particularly useful when defining templates on top of Smart Data Loader based Complex Features. See the tutorial section.
