package com.tsige.st.easytachos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class CargarCamionesGOST {

	
	private static String gost = "192.168.1.34";

	public static void main(String[] args) {
		// TODO ESTA HARDCODEADO PARA CREAR UN SOLO CAMION, PARA LA VERSION FINAL AGREGAR MAS CAMIONES
		Double x = -56.12416;
		Double y =  -34.898242;
    	String json = obtenerJSONCamion(1,x,y);
    	send(json);
	}
	
	public static String obtenerJSONCamion (int id, double x, double y) {
		String post = "{\r\n" + 
				"  \"name\": \"Camion <id>\",\r\n" + 
				"  \"description\": \"Camion <id>\",\r\n" + 
				"  \"Locations\": [{\r\n" + 
				"    \"name\": \"Ubicacion camion <id>\",\r\n" + 
				"    \"description\": \"Camion <id>\",\r\n" + 
				"    \"encodingType\": \"application/vnd.geo+json\",\r\n" + 
				"    \"location\": {\r\n" + 
				"      \"type\": \"Point\",\r\n" + 
				"      \"coordinates\": [ <x>,<y>]\r\n" + 
				"    }\r\n" + 
				"  }]\r\n" + 				
				"}";
		
		post = post.replaceAll("<id>", String.valueOf(id));
		post = post.replaceAll("<x>", String.valueOf(x));
		post = post.replaceAll("<y>", String.valueOf(y));
				
		return post;
	}
	
	public static void send(String input) {
		
		try {
			URL url = new URL("http://"+gost+":8080/v1.0/Things");
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
