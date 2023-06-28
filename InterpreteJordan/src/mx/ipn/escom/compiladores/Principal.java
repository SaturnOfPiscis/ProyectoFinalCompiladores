package mx.ipn.escom.compiladores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

public class Principal {

    static boolean existenErrores = false;

    public static void main(String[] args) throws IOException, Exception {
        if (args.length > 1) {
            System.out.println("Uso correcto: interprete [script]");

            // Convención definida en el archivo "system.h" de UNIX
            System.exit(64);
        } else if (args.length == 1) {
            ejecutarArchivo(args[0]);
        } else {
            ejecutarPrompt();
        }
    }

    private static void ejecutarArchivo(String path) throws IOException, ParseException, Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        ejecutar(new String(bytes, Charset.defaultCharset()));

        // Se indica que existe un error
        if (existenErrores) {
            System.exit(65);
        }
    }

    private static void ejecutarPrompt() throws IOException, ParseException, Exception {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print(">>> ");
            String linea = reader.readLine();
            if (linea == null)
                break; // Presionar Ctrl + D
            ejecutar(linea);
            existenErrores = false;
        }
    }

    private static void ejecutar(String source) throws ParseException, Exception {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }

        Parser parser = new Parser(tokens);
        parser.parse();

        GeneradorPostfija gpf = new GeneradorPostfija(tokens);
        List<Token> postfija = gpf.convertir();

        for (Token token : postfija) {
            System.out.println(token);
        }

        GeneradorAST gast = new GeneradorAST(postfija);
        Arbol programa = gast.generarAST();
        programa.recorrer();
    }

    /*
     * El método error se puede usar desde las distintas clases para reportar los
     * errores: Interprete.error(....);
     */
    static void error(int linea, String mensaje) {
        reportar(linea, "", mensaje);
    }

    private static void reportar(int linea, String donde, String mensaje) {
        System.err.println("[linea " + linea + "] Error " + donde + ": " + mensaje);
        existenErrores = true;
    }

}