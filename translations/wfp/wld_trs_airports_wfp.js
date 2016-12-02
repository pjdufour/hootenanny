function expand(x)
{
  var newArray = [];
  if(Array.isArray(x))
  {
    for(var i = 0; i < x.length; i++)
    {
      var value = x[i];
      if((typeof value) == "string")
      {
        if(value.indexOf(".") != -1)
        {
          newArray = newArray.concat(value.split("."));
        }
        else
        {
          newArray.push(value);
        }
      }
      else
      {
        newArray.push(value);
      }
    }
  }
  else if((typeof x) == "string")
  {
    newArray = x.split(".");
  }
  return newArray;
}

function extract(keyChain, node, fallback)
{
  if((typeof keyChain) == "string")
  {
    keyChain = keyChain.split(".");
  }

  var obj = undefined;

  if(node != undefined && node != null)
  {
    if(keyChain.length==0)
    {
      obj = node;
    }
    else
    {
      var newKeyChain = keyChain.slice(1);
      if(newKeyChain.length == 0)
      {
        if(((typeof keyChain[0]) == "string") && keyChain[0].toLowerCase() == "length")
        {
          if(Array.isArray(node))
          {
            obj = node.length;
          }
          else if(node != undefined)
          {
            obj = node["length"];
          }
          else
          {
            obj = 0;
          }
        }
      }

      if(obj == undefined && node != undefined)
      {
        if(Array.isArray(node))
        {
          var index = ((typeof keyChain[0]) == "string") ?
            parseInt(keyChain[0], 10) :
            keyChain[0];
          obj = extract(newKeyChain, node[index], fallback);
        }
        else
        {
          obj = extract(newKeyChain, node[""+keyChain[0]], fallback);
        }
      }
  	}
  }
  else
  {
    obj = fallback;
  }
	return obj;
}

function translateAttributes(attrs, layerName, geometryType)
{
  return attrs;
}

function translateToOgr(tags, elementType, geometryType)
{
  if(elementType != 'Node')
  {
    return null;
  }

  var attrs = {
    "namelong": (extract("wfp:namelong", tags) || extract("name", tags) || extract("name:en", tags) || extract("int_name", tags)),
    "nameshort": (extract("wfp:nameshort", tags) || extract("name", tags) || extract("name:en", tags) || extract("int_name", tags)),
    "namealt": (extract("wfp:namealt", tags) || extract("name", tags) || extract("name:en", tags) || extract("int_name", tags)),
    "icao": extract("icao", tags, ""),
    "iata": extract("iata", tags, ""),
    "elevm": extract("wfp:elevm", tags) || extract("ele", tags, "")
  }
  return { attrs: attrs, tableName: "wld_trs_airports_wfp" };
}

function getDbSchema()
{
  var columns = [
    {"name": "namelong", "type": "String", "defValue": ""},
    {"name": "nameshort", "type": "String", "defValue": ""},
    {"name": "namealt", "type": "String", "defValue": ""},
    {"name": "icao", "type": "String", "defValue": ""},
    {"name": "iata", "type": "String", "defValue": ""},
    {"name": "elevm", "type": "String", "defValue": ""}
  ];
  var schema = [
    {
        name: 'wld_trs_airports_wfp',
        geom: 'Point',
        columns: columns
    }
  ];
  return schema;
};
