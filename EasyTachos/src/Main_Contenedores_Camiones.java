
import java.io.IOException;

public class Main_Contenedores_Camiones {

	public static void main(String[] args) throws IOException {
		
		System.out.println("Iniciando...");
		ControladorBL singleton = ControladorBL.getInstance();
		singleton.cargar();
		System.out.println("Carga finalizada...");
	}

}
