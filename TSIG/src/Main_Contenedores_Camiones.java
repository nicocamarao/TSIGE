import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main_Contenedores_Camiones {

	public static void main(String[] args) throws IOException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		
		System.out.println(dtf.format(now) + "Iniciando...");
		ControladorBL.getInstance();
		System.out.println(dtf.format(now) + ": Carga finalizada...");
		System.console().readLine();
	}

}
