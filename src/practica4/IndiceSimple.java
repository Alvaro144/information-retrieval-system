package practica4;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.document.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class IndiceSimple {
    
    static final char SEPARATOR=';';
    static final char QUOTE='"';
    // Ruta donde se almacenará el índice
    String rutaindice = "C:\\Users\\Álvaro\\Desktop\\Facultad\\RI\\Práctica 4\\salidas";
    // Ruta donde se almacenarán las facetas
    String rutafacetas = "C:\\Users\\Álvaro\\Desktop\\Facultad\\RI\\Práctica 4\\facetas";
    // Array donde se almacenarán los ficheros
    File[] docPath;
    FSDirectory directoriofacetas = FSDirectory.open(Paths.get(rutafacetas));
    
    Analyzer analyzer;
    Similarity similarity;

    // Clase que se encarga de la indexación. Permite añadir,borrar o modificar
     //documentos 
    private IndexWriter writer;
    
    // Un documento Lucene está compuesto por un par Campo-Contenido como
    // por ejemplo Autor-Carlos
    
    // Para poder utilizar un conjunto de analyzers para cada tipo de dato
    static HashMap<String, Analyzer> analyzers = new HashMap<>();
    static PerFieldAnalyzerWrapper perfieldanalyzer = null;
    
    // Utilizaremos las categorias para clasificar documentos. por ejemplo, las 
    // categorias de un libro pueden ser su precio, autor, etc.
    // Además, podemos encontrar subcategorias, como un rango de precios o autores
    // que son relevantes
    
    // Las búsquedas por campos se limitan a un solo campo, como autor:"Mark"
    // para solucionar esto (no se saben los campos que tiene el sistema) se 
    // proporciona una lista de subcategorias útiles.
    
    // Donde se almacenan las configuraciones de las facetas
    FacetsConfig fconfig = new FacetsConfig();
    // Donde se almacena la información de la taxonomía en el disco y mantiene una
    // cache en memoria de algunas o todas las categorías
    DirectoryTaxonomyWriter taxoWriter = 
            new DirectoryTaxonomyWriter(directoriofacetas);
    
//*******************************       CONSTRUCTOR ****************************  
    
    IndiceSimple(File[] file) throws IOException{
        
        docPath = file;
        
    }
    
//***************************** CONDIGURAR EL ÍNDICE ***************************
    
    public void configurarIndice(Similarity similarity) throws IOException{
        this.similarity = similarity;
        // Añadimos para cada tipo de dato su analyzer
        analyzers.put("Autor", new CustomAnalyzerAutor());
        analyzers.put("Titulo",new EnglishAnalyzer());
        analyzers.put("AuthorKeywords",new CustomAnalyzerKey());
        analyzers.put("IndexKeywords",new CustomAnalyzerKey());
        analyzers.put("Resumen",new EnglishAnalyzer());
        analyzers.put("Todo",new EnglishAnalyzer());
        
        // WhiteSpaceAnalyzer se utilizará para todos los campos menos para los 
        // que están en el hashmap "analyzers"
        analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(),analyzers);
        // Donde se almacenará el índice. FSDirectory significa que es en disco
        Directory dir = FSDirectory.open(Paths.get(rutaindice));
        // Esto almacena la configuración: se utilizará el analizador que le 
        // hemos pasado
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(similarity);
        // Modo CREATE: crear o sobreescribir
        // Modo CREATE_OR_APPEND: permite añadir documentos al fichero sin rehacerlo
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        
        // Toma como entrada el directorio donde se almacenará el indice y la
        // configuración utilizada
        writer = new IndexWriter(dir,iwc);
        
        //Configuraciones para las facetas
        fconfig.setMultiValued("AñoInt",true);
        fconfig.setMultiValued("Autor",true);
        fconfig.setMultiValued("AuthorKeywords",true);
        fconfig.setMultiValued("IndexKeywords",true);
        
    }
    
    public void indexarDocumentos() throws IOException, CsvException{
        
        // Para cada uno de los documentos a insertar
        for(File f : docPath){
            
            //Recogemos la ruta del fichero
            String ruta = f.getAbsolutePath();

            //Leer el documento y devolverlo en una lista de String (Cada campo 
            // en una posicion)
            try(CSVReader reader = new CSVReader(new FileReader(ruta))){
                //docastring = reader.readAll();
                String[] nextRecord;
                // Nos saltamos la cabecera
                reader.skip(1);
                while ((nextRecord = reader.readNext()) != null) {
                    // UN DOCUMENTO POR CADA LINEA DEL CSV
                    Document doc = new Document();
                    String todo = nextRecord[0].concat(nextRecord[2]).concat(" ").concat(nextRecord[3]).concat(" ").concat(nextRecord[13]).concat(" ").concat(nextRecord[16]).concat(" ").concat(nextRecord[17]).concat(" ").concat(nextRecord[18]);
                    // TextField: secuencia de terminos tokenizada,etc
                    doc.add(new TextField("Autor",nextRecord[0],Field.Store.YES));
                    doc.add(new TextField("Titulo",nextRecord[2],Field.Store.YES));
                    doc.add(new TextField("Año",nextRecord[3],Field.Store.YES));
                    doc.add(new IntPoint("AñoInt",Integer.parseInt(nextRecord[3])));
                    // Nombre, valor , tipo(si se gurda o no)
                    doc.add(new TextField("Resumen",nextRecord[16],Field.Store.YES));
                    doc.add(new TextField("AuthorKeywords",nextRecord[17],Field.Store.YES));
                    doc.add(new TextField("IndexKeywords",nextRecord[18],Field.Store.YES));
                    doc.add(new TextField("Todo",todo,Field.Store.YES));
                    
                    // Arrays para recoger los distintos campos
                    ArrayList<String> Autores_fac = obtenerAutores(nextRecord[0]);
                    ArrayList<String> IndexKeys_fac = obtenerKeyWords(nextRecord[18]);
                    ArrayList<String> AutorKeys_fac = obtenerAutorKeywords(nextRecord[17]);

                    
                    // Esta faceta se guardará en formato String
                    doc.add(new FacetField("Año",nextRecord[3]));

                    for(int i=1;i<Autores_fac.size();i++){
                        doc.add(new FacetField("Autor",Autores_fac.get(i)));

                    }
                    for(int i=1;i<IndexKeys_fac.size();i++){
                        doc.add(new FacetField("IndexKeywords",IndexKeys_fac.get(i)));

                    }
                    for(int i=1;i<AutorKeys_fac.size();i++){
                        doc.add(new FacetField("AuthorKeywords",AutorKeys_fac.get(i)));
                    }
                    
                    writer.addDocument(fconfig.build(taxoWriter,doc));
                    
                }
                reader.close(); 
            }
            
        }
        close();
    }
    
    public void close(){
        try{
            // Realizar cambios pendientes
            writer.commit();
            // Cerrrar indicebiswas
            writer.close();
            taxoWriter.close();
            
        }catch(IOException e){
            System.out.println("error al cerrar");
        }
    }

    private ArrayList obtenerAutores(String lista){
        // Aqui almacenaremos los autores como ArrayList
        ArrayList autores = new ArrayList();
        // Predefinido como no-autor
        String composicion_nombre = "no-autor";

        // Si hay una coma, acaba el autor y empieza el siguiente
        for(int i=0; i<lista.length(); i++){
            //Si el caracter no es una ,
            if(!String.valueOf(lista.charAt(i)).equals(",")){
                // Añadimos el siguiente caracter
                composicion_nombre += String.valueOf(lista.charAt(i));
            }
            else{
                // Si es una , añadimos el nombre al array eliminando caracteres
                // en blanco del inicio y final (trim()) y pasamos a minusculas
                autores.add(composicion_nombre.trim().toLowerCase());
                // Reiniciamos el array del nombre
                composicion_nombre = "";
            }
        }
        return autores;
    }
    
    private ArrayList obtenerKeyWords(String lista){
        
        // Aquí almacenamos los keywords
        ArrayList<String> keys = new ArrayList<>();
        String aux="no-keys";

        for(int i=0; i<lista.length(); i++){
            //Si el caracter no es una ,
            if(!String.valueOf(lista.charAt(i)).equals(";")){
                // Añadimos el siguiente caracter
                aux += String.valueOf(lista.charAt(i));
            }
            else{
                // Si es una , añadimos el nombre al array eliminando caracteres
                // en blanco del inicio y final (trim()) y pasamos a minusculas
                keys.add(aux.trim().toLowerCase());
                // Reiniciamos el array del nombre
                aux = "";
            }
        }
        if(lista.length() == 0){
            keys.add(aux);
        }

        return keys;

    }

    
    private ArrayList obtenerAutorKeywords(String lista){
        
        // Aquí almacenamos los keywords
        ArrayList<String> keys = new ArrayList<>();
        String aux="no-autorKeys";
        
        for(int i=0; i<lista.length(); i++){
            //Si el caracter no es una ,
            if(!String.valueOf(lista.charAt(i)).equals(";")){
                // Añadimos el siguiente caracter
                aux += String.valueOf(lista.charAt(i));
            }
            else{
                // Si es una , añadimos el nombre al array eliminando caracteres
                // en blanco del inicio y final (trim()) y pasamos a minusculas
                keys.add(aux.trim().toLowerCase());
                // Reiniciamos el array del nombre
                aux = "";
            }
        }

        return keys;

    }
    
}
