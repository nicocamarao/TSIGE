import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONObject;

public class MiContenedor implements Runnable {

	private String estado;
	private double capacidad;
	private int thing;
	private int datastream;
	private String gost = "18.231.190.192";

	public MiContenedor(int thing, int datastream) {
		estado = "DISPONIBLE";
		this.thing = thing;
		this.datastream = datastream;
		capacidad = 0;
	}

	public void run() {

		try {
			while (true) {
				long randomtime = (long) ThreadLocalRandom.current().nextDouble(1000, 10000);
				Thread.sleep(randomtime);
				// capacidad
				double random = ThreadLocalRandom.current().nextDouble(0, 1);
				if (estado.equals("DISPONIBLE")) {
					double nuevo = capacidad + random;
					if (nuevo >= 100) {
						estado = "LLENO";
						capacidad = 100;
						updateCapacidadEnGOST(capacidad);
					} else {
						capacidad = nuevo;
						updateCapacidadEnGOST(capacidad);
					}
				} else if (estado.equals("CAMION")) {
					capacidad = 0;
					estado = "DISPONIBLE";
					updateCapacidadEnGOST(capacidad);
				}
				double temperatura = ThreadLocalRandom.current().nextDouble(21, 34);
				if(temperatura > 33.900)
				{
					//Alerta();
				}

			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	
	public void updateCapacidadEnGOST(Double capacidad) throws IOException
	{
		String u = "http://" + gost + ":9080/v1.0/Datastreams(" + datastream + ")/Observations/";
		URL url = new URL(u);
		JSONObject body = new JSONObject();
		body.put("capacidad", capacidad);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(/*proxy*/);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		
		
		OutputStream wr= conn.getOutputStream();		
		
		wr.write(body.toString().getBytes());
		wr.flush();
		
		if (conn.getResponseCode() != 201) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}		
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));
		String output = "";
		String jstring = "";
		while ((output = br.readLine()) != null) {
			jstring += output;
		}		
		System.out.println("CAPACIDAD ENVIADA AL SERVIDOR, RESPUESTA\n"+jstring);
		conn.disconnect();		
		
		
	}
}
