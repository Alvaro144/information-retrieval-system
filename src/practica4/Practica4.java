package practica4;

import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;

public class Practica4 {

    public static void main(String[] args) throws IOException, CsvException, 
            ParseException{

        File carpeta = new File(args[0]);
        // Almacenamos en un array de files los diferentes ficheros
        File[] listado = carpeta.listFiles();
        // Instanciamos 
        IndiceSimple baseline = new IndiceSimple(listado);
        // Conrfiguramos el índice con BM25
        baseline.configurarIndice(new BM25Similarity());
        // Indexamos los documentos
        baseline.indexarDocumentos();
        //Creamos interfaz y la hacemos visible
        Interfaz interfaz = new Interfaz();
        interfaz.setVisible(true);
    }
    
}
