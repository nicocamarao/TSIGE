package fing.tsig.metodos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

	public static void main(String[] args) {
		
		String archivo = args[0];
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(archivo));
			JSONObject jsonObject =  (JSONObject) obj;
			String name = (String) jsonObject.get("name");
	        System.out.println(name);
	        Double x = 0D;
	        Double y = 0D;
	        String barrio = "";
	        JSONArray features = (JSONArray) jsonObject.get("features");
	        int idContenedor = 0;
	        for (Object a : features) {
	        	idContenedor++;
	        	JSONObject feature = (JSONObject) a;
	        	JSONObject properties = (JSONObject)feature.get("properties");
	        	barrio = (String)properties.get("nombbarr");
	        	JSONObject geometria = (JSONObject)feature.get("geometry");	   
	        	JSONArray coordenadas = (JSONArray) geometria.get("coordinates");
	        	x = (Double)coordenadas.get(0);
	        	y =  (Double)coordenadas.get(1);
	        	 String json = obtenerJSON(idContenedor,x,y,barrio);
	        	 send(json);
	        }
	        
	        
	        
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
	
	
	public static String obtenerJSON (int id, double x, double y, String barrio) {
		String post = "{\r\n" + 
				"  \"name\": \"Contenedor <id>\",\r\n" + 
				"  \"description\": \"Contenedor de residuos especiales <id>\",\r\n" + 
				"  \"properties\": {\r\n" + 
				"    \"barrio\": \"<barrio>\"\r\n" + 
				"  },\r\n" + 
				"  \"Locations\": [{\r\n" + 
				"    \"name\": \"Ubicacion contenedor <id>\",\r\n" + 
				"    \"description\": \"Contenedor especial <id>\",\r\n" + 
				"    \"encodingType\": \"application/vnd.geo+json\",\r\n" + 
				"    \"location\": {\r\n" + 
				"      \"type\": \"Point\",\r\n" + 
				"      \"coordinates\": [ <x>,<y>]\r\n" + 
				"    }\r\n" + 
				"  }],\r\n" + 
				"  \"Datastreams\": [{\r\n" + 
				"    \"name\": \"Datastream Temperatura <id>\",\r\n" + 
				"    \"description\": \"Datastream para registrar temperatura contenedor <id>\",\r\n" + 
				"    \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\r\n" + 
				"    \"unitOfMeasurement\": {\r\n" + 
				"      \"name\": \"Grados Celsius\",\r\n" + 
				"      \"symbol\": \"degC\",\r\n" + 
				"      \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeCelsius\"\r\n" + 
				"    },\r\n" + 
				"    \"ObservedProperty\": {\r\n" + 
				"      \"name\": \"Temperatura\",\r\n" + 
				"      \"description\": \"Temperatura interna del contenedor <id>\",\r\n" + 
				"      \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#AreaTemperature\"\r\n" + 
				"    },\r\n" + 
				"    \"Sensor\": {\r\n" + 
				"      \"name\": \"Sensor Temp <id>\",\r\n" + 
				"      \"description\": \"Sensor de temperatura contenedor <id>\",\r\n" + 
				"      \"encodingType\": \"application/pdf\",\r\n" + 
				"      \"metadata\": \"Sensor de temperatura\"\r\n" + 
				"    }\r\n" + 
				"  },{\r\n" + 
				"    \"name\": \"Datastream Capacidad <id>\",\r\n" + 
				"    \"description\": \"Datastream para registrar capacidad contenedor <id>\",\r\n" + 
				"    \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\r\n" + 
				"    \"unitOfMeasurement\": {\r\n" + 
				"      \"name\": \"Centimetro\",\r\n" + 
				"      \"symbol\": \"cm\",\r\n" + 
				"      \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Centimeter\"\r\n" + 
				"    },\r\n" + 
				"    \"ObservedProperty\": {\r\n" + 
				"      \"name\": \"Capacidad\",\r\n" + 
				"      \"description\": \"Capacidad de contenedor <id>\",\r\n" + 
				"      \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#Length\"\r\n" + 
				"    },\r\n" + 
				"    \"Sensor\": {\r\n" + 
				"      \"name\": \"Sensor Capacidad <id>\",\r\n" + 
				"      \"description\": \"Sensor de capacidad contenedor <id>\",\r\n" + 
				"      \"encodingType\": \"application/pdf\",\r\n" + 
				"      \"metadata\": \"Sensor de capacidad\"\r\n" + 
				"    }\r\n" + 
				"  }]\r\n" + 
				"}";
		
		post = post.replaceAll("<id>", String.valueOf(id));
		post = post.replaceAll("<x>", String.valueOf(x));
		post = post.replaceAll("<y>", String.valueOf(y));
		post = post.replaceAll("<barrio>", barrio);
				
		return post;
	}
	
	public static void send(String input) {
		
		try {

			URL url = new URL("http://192.168.1.34:8080/v1.0/Things");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		 }

		}
		
	

}
