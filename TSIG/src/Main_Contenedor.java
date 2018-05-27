import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



	

	public class Main_Contenedor {
	
	public static void main(String[] args) throws IOException {
		
		List<String[]> contenedores = new ArrayList<String[]>();
		contenedores.add(new String[]{"1","LOCATIONFAKE","0", "34", "DISPONIBLE"});
		contenedores.add(new String[]{"2","LOCATIONFAKE","98", "33", "CAMION"});
		contenedores.add(new String[]{"3","LOCATIONFAKE","0", "29", "DISPONIBLE"});
		contenedores.add(new String[]{"4","LOCATIONFAKE","0", "26", "DISPONIBLE"});
		contenedores.add(new String[]{"5","LOCATIONFAKE","0", "26", "DISPONIBLE"});
		contenedores.add(new String[]{"6","LOCATIONFAKE","100", "34", "LLENO"});
		for(int i=0;i<contenedores.size();i++){
		 MiContenedor myRunnable = new MiContenedor(contenedores.get(0));
	        Thread t = new Thread(myRunnable);
	        t.start();
		}
		
		

	}

	
}
