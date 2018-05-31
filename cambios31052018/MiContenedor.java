
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONObject;

public class MiContenedor implements Runnable {

	private String estado;
	private Double capacidad;
	private int thing;
	private int datastreamCapacidad;
	private int datastreamTemperatura;
	private String gost = "18.231.190.192";
	private String barrio = "";

	public MiContenedor(int thing, int dsCap, int dsTemp, String barrio) {
		this.estado = "DISPONIBLE";
		this.thing = thing;
		this.datastreamCapacidad = dsCap;
		this.datastreamTemperatura = dsTemp;
		this.capacidad = 0.0;
		this.barrio  = barrio;
	}
	
	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	public Double getCapacidad (){
		return capacidad;
	}
	
	public String getBarrio (){
		return barrio;
	}
	
	public void run() {

		long randomtime = 0;
		try {
			while (true) {
				randomtime += (long) ThreadLocalRandom.current().nextDouble(1000, 5000)*20;
				if (thing == 224)
					System.out.println("Tiempo "+randomtime+" Capacidad "+capacidad);
				Thread.sleep(randomtime/20);
				// capacidad
				Double random = ThreadLocalRandom.current().nextDouble(0, 1);
				if (estado.equals("DISPONIBLE")) {
					Double nuevo = capacidad + random;
					if (nuevo >= 100) {
						estado = "LLENO";
						capacidad = 100.0;
						if (randomtime > 4*60*1000) {
							randomtime = 0;
							updateCapacidadEnGOST(capacidad);
						}
					} else {
						capacidad = nuevo;
						if (randomtime > 5*60*1000) {
							randomtime = 0;
							updateCapacidadEnGOST(capacidad);
						}
					}
				} else if (estado.equals("CAMION")) {
					capacidad = 0.0;
					estado = "DISPONIBLE";
					if (randomtime > 2*60*1000) {
						randomtime = 0;
						updateCapacidadEnGOST(capacidad);
					}
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
		String u = "http://" + gost + ":9080/v1.0/Datastreams(" + datastreamCapacidad + ")/Observations";
		URL url = new URL(u);
		JSONObject body = new JSONObject();
		body.put("result", capacidad);
		//Proxy proxy = new Proxy(Proxy.Type.HTTP, new
		//		InetSocketAddress("proxysis", 8080));
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
		//System.out.println("CAPACIDAD ENVIADA AL SERVIDOR, RESPUESTA\n"+jstring);
		conn.disconnect();		
		
		
	}
}
