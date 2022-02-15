package practica4;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.DrillSideways.DrillSidewaysResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;


public class Buscar {
   
    // Ruta índice
    String rutaindice = "C:\\Users\\Álvaro\\Desktop\\Facultad\\RI\\Práctica 4\\salidas";
    // Ruta donde se almacenarán las facetas
    String rutafacetas = "C:\\Users\\Álvaro\\Desktop\\Facultad\\RI\\Práctica 4\\facetas";
    
    // El indexSearcher será el encargado de computar la similitud entre los 
    // documentos y la consulta. Se usa el BM25 omo predeterminado.
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(rutaindice))));
    
    // Contruimos la consulta a partir de todas las booleanclause
    BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
    // Almacenamos la consulta final a partir del bqbuilder
    BooleanQuery total_consultas; 

    // Para leer el ídice invertido 
    DirectoryReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(rutaindice)));
 
    // Para leer el índice de las facetas
    TaxonomyReader taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(Paths.get(rutafacetas)));
    
    FacetsCollector fc = new FacetsCollector();
    FacetsConfig fconfig = new FacetsConfig();
    Facets facets;
    
    
    DrillDownQuery ddq;
    
    // Para busqueda por frases
    PhraseQuery pq;
    PhraseQuery.Builder builder_frase = new PhraseQuery.Builder();
    
    String [] campos;
    String [] campos_excluidos;
    //***********************       CONSTRUCTOR **********************
    
    Buscar(String [] campos, String [] campos_excluidos) 
            throws ParseException, IOException{
        
        // Inicializamos los campos por los que busca el usuario 
        this.campos = campos;
        this.campos_excluidos = campos_excluidos;

        //Configuraciones para las facetas
        fconfig.setMultiValued("AñoInt",true);
        fconfig.setMultiValued("Autor",true);
        fconfig.setMultiValued("AuthorKeywords",true);
        fconfig.setMultiValued("IndexKeywords",true);

    }

    public void configurar_busqueda() throws IOException{  
                
        
        // 0 -> autor
        // 1 -> titulo
        // 2 -> autor keywords
        // 3 -> index keyowrds
        // 4 -> año
        // 5 -> resumen
        
        
        //*********** BUSCA POR AUTOR ***********
        //if(consultas[0]){
          if(campos[0] != null){  
            // Le pasaremos el mismo analizer que al indexar
            Analyzer analyzer_para_conteo = new CustomAnalyzerAutor();
            Analyzer analyzer = new CustomAnalyzerAutor();
            TokenStream resumen_tokenizado_para_conteo = analyzer_para_conteo.tokenStream(null,campos[0]);
            TokenStream resumen_tokenizado = analyzer.tokenStream(null,campos[0]);
            
            // Vemos cuantas palabras hay
            int tamaño = 0;
            resumen_tokenizado_para_conteo.reset();
            while(resumen_tokenizado_para_conteo.incrementToken()){
                tamaño++;
            }
            
            // Contador para ir cogiendo las posiciones del vector de campos
            int contador_tokens = 0;
            resumen_tokenizado.reset();
            while(resumen_tokenizado.incrementToken()){    
                // Añadimos al constructor una booleanClause, que está formada
                // por un TermQuery que a su vez está formado por un Term
                bqbuilder.add(new BooleanClause(new TermQuery (new Term("Autor",resumen_tokenizado.getAttribute((Class<CharTermAttribute>) CharTermAttribute.class).toString())),BooleanClause.Occur.SHOULD));
                contador_tokens++;
            }    
        }
        
        //*********** BUSCA POR TITULO ***********
          if(campos[1] != null){
              // Le pasaremos el mismo analizer que al indexar
            Analyzer analyzer_para_conteo = new EnglishAnalyzer();
            Analyzer analyzer = new EnglishAnalyzer();
            TokenStream resumen_tokenizado_para_conteo = analyzer_para_conteo.tokenStream(null,campos[1]);
            TokenStream resumen_tokenizado = analyzer.tokenStream(null,campos[1]);
            
            // Vemos cuantas palabras hay
            int tamaño = 0;
            resumen_tokenizado_para_conteo.reset();
            while(resumen_tokenizado_para_conteo.incrementToken()){
                tamaño++;
            }
            
            // Contador para ir cogiendo las posiciones del vector de campos
            int contador_tokens = 0;
            resumen_tokenizado.reset();
            while(resumen_tokenizado.incrementToken()){    
                // Añadimos al constructor una booleanClause, que está formada
                // por un TermQuery que a su vez está formado por un Term
                bqbuilder.add(new BooleanClause(new TermQuery (new Term("Titulo",resumen_tokenizado.getAttribute((Class<CharTermAttribute>) CharTermAttribute.class).toString())),BooleanClause.Occur.SHOULD));
                contador_tokens++;
            } 
        }
        
        //*********** BUSCA POR AUTOR KEYWORDS ***********
        //if(consultas[2]){
        if(campos[2] != null){
            String[] campos_split = campos[2].split(" ");
            int tamaño = campos_split.length;
            for(int i = 0;i<tamaño;i++){
                bqbuilder.add(new BooleanClause(new TermQuery (new Term("AuthorKeywords",campos_split[i].toLowerCase())),BooleanClause.Occur.SHOULD));
            }
        }
        
        //*********** BUSCA POR INDEX KEYWORDS ***********
        if(campos[3] != null){
            String[] campos_split = campos[3].split(" ");
            int tamaño = campos_split.length;
            for(int i = 0;i<tamaño;i++){
                bqbuilder.add(new BooleanClause(new TermQuery (new Term("IndexKeywords",campos_split[i].toLowerCase())),BooleanClause.Occur.SHOULD));
            }
        }
        
        //*********** BUSCA POR AÑO ***********
        if(campos[4] != null){
            bqbuilder.add(new BooleanClause(IntPoint.newExactQuery("AñoInt",Integer.parseInt(campos[4])),BooleanClause.Occur.SHOULD));  
        }
        
        //*********** BUSCA POR RESUMEN ***********
        if(campos[5] != null){
            
            // Analyzers para dejar dejarlo igual que en el índice
            Analyzer analyzer_para_conteo = new EnglishAnalyzer();
            Analyzer analyzer = new EnglishAnalyzer();
            TokenStream resumen_tokenizado_para_conteo = analyzer_para_conteo.tokenStream(null,campos[5]);
            TokenStream resumen_tokenizado = analyzer.tokenStream(null,campos[5]);

            // Vemos el tamaño del texto
            int tamaño = 0;
            resumen_tokenizado_para_conteo.reset();
            while(resumen_tokenizado_para_conteo.incrementToken()){
                tamaño++;
            }

            // Copiamos los tokens del tokenStream en un String
            String [] terminos_resumen = new String[tamaño];
            int contador_tokens = 0;
            resumen_tokenizado.reset();
            while(resumen_tokenizado.incrementToken()){
                terminos_resumen[contador_tokens] = resumen_tokenizado.getAttribute((Class<CharTermAttribute>) CharTermAttribute.class).toString();
                contador_tokens++;
            }
            // Creamos las clausulas
            for(int i = 0;i<tamaño;i++){
                bqbuilder.add(new BooleanClause(new TermQuery (new Term("Resumen",terminos_resumen[i])),BooleanClause.Occur.SHOULD));
            }          
        }
        if(campos[6] != null){
            
            // Analyzers para dejar dejarlo igual que en el índice
            Analyzer analyzer_para_conteo = new EnglishAnalyzer();
            Analyzer analyzer = new EnglishAnalyzer();
            TokenStream resumen_tokenizado_para_conteo = analyzer_para_conteo.tokenStream(null,campos[6]);
            TokenStream resumen_tokenizado = analyzer.tokenStream(null,campos[6]);

            // Vemos el tamaño del texto
            int tamaño = 0;
            resumen_tokenizado_para_conteo.reset();
            while(resumen_tokenizado_para_conteo.incrementToken()){
                tamaño++;
            }

            // Copiamos los tokens del tokenStream en un String
            String [] terminos_resumen = new String[tamaño];
            int contador_tokens = 0;
            resumen_tokenizado.reset();
            while(resumen_tokenizado.incrementToken()){
                terminos_resumen[contador_tokens] = resumen_tokenizado.getAttribute((Class<CharTermAttribute>) CharTermAttribute.class).toString();
                contador_tokens++;
            }

            // Creamos las clausulas
            for(int i = 0;i<tamaño;i++){
                bqbuilder.add(new BooleanClause(new TermQuery (new Term("Todo",terminos_resumen[i])),BooleanClause.Occur.SHOULD));
            }
        } 
        
        
        
        //*************** PARA LAS EXCLUYENTES ******************
        
        //*********** EXCLUIR AUTOR ***********
        if(campos_excluidos[0] != null ){
            
            // Le pasaremos el mismo analizer que al indexar
            Analyzer analyzer_para_conteo = new CustomAnalyzerAutor();
            Analyzer analyzer = new CustomAnalyzerAutor();
            TokenStream resumen_tokenizado_para_conteo = analyzer_para_conteo.tokenStream(null,campos_excluidos[0]);
            TokenStream resumen_tokenizado = analyzer.tokenStream(null,campos_excluidos[0]);
            // Vemos el tamaño del texto
            int tamaño = 0;
            resumen_tokenizado_para_conteo.reset();
            while(resumen_tokenizado_para_conteo.incrementToken()){
                tamaño++;
            }
            // Copiamos los tokens del tokenStream en un String
            String [] terminos_resumen = new String[tamaño];
            int contador_tokens = 0;
            resumen_tokenizado.reset();
            while(resumen_tokenizado.incrementToken()){
                terminos_resumen[contador_tokens] = resumen_tokenizado.getAttribute((Class<CharTermAttribute>) CharTermAttribute.class).toString();
                contador_tokens++;
            }
            // Creamos las clausulas
            for(int i = 0;i<tamaño;i++){

                //Añadimos las clausulas al constructor general
                bqbuilder.add(new BooleanClause(new TermQuery (new Term("Autor",terminos_resumen[i])),BooleanClause.Occur.MUST_NOT));
            }     
        }

        
        //*********** EXCLUIR TITULO ***********
        if(campos_excluidos[1] != null){
            String[] campos_split = campos_excluidos[1].split(" ");
            int tamaño = campos_split.length;
            for(int i = 0;i<tamaño;i++){
                bqbuilder.add(new BooleanClause(new TermQuery (new Term("Titulo",campos_split[i].toLowerCase())),BooleanClause.Occur.MUST_NOT));
            }
        }

        //*********** EXLUIR AÑO ***********
        if(campos_excluidos[2] != null){
            bqbuilder.add(new BooleanClause(IntPoint.newExactQuery("AñoInt",Integer.parseInt(campos_excluidos[2])),BooleanClause.Occur.MUST_NOT));      
        }
        
        
        // Construimos la consulta con todas las consultas almacenadas en el bqbuilder
        total_consultas = bqbuilder.build();
        
    }
    
    // Devuelve un TopDocs, que es un objeto con los documentos devueltos por la 
    // consulta
    public TopDocs get_TopDocs_consulta() throws IOException{
        return searcher.search(total_consultas, 50);    
    }
    
    public ArrayList<Integer> ocurrencias() throws IOException{
        TopDocs results;
        // Sacamos todos los documentos que emparejan
        results = searcher.search(total_consultas, 20);
        
        ScoreDoc[] hits = results.scoreDocs;
        int contador = 0;
        int pos_doc = 0;
        // Almacenamos en un String las posiciones de los docs que emparejan con
        // todas las palabras
        ArrayList<Integer> posiciones = new ArrayList();
        for(ScoreDoc hit : hits){
            if(searcher.explain(total_consultas, hit.doc).toString().length() > 900){
                posiciones.add(pos_doc);
                pos_doc++;
                contador++;
            }else{
                pos_doc++;
            } 
        }
        return posiciones;
    }
    
    public ArrayList<String> get_Explanation(boolean filtrada) throws IOException{
        TopDocs results;
        if(filtrada){
            results = searcher.search(ddq, 20);
        }else{
            results = searcher.search(total_consultas, 20);
        }
        ScoreDoc[] hits = results.scoreDocs;
        ArrayList<String> explanation = new ArrayList();
        for(ScoreDoc hit : hits){
            explanation.add(searcher.explain(total_consultas, hit.doc).toString());
        }
        return explanation;
    }

     
    public Facets get_facetas() throws IOException{
        // Guardamos las facetas en fc
        FacetsCollector.search(searcher, total_consultas, 20, fc);
        Facets facetas = new FastTaxonomyFacetCounts(taxoReader,fconfig,fc);
        return facetas;
    }
    
    public DrillSidewaysResult  gestionar_filtros(String [] filtros) throws IOException{
        
        ddq = new DrillDownQuery(fconfig,total_consultas);
        if(filtros[0] != null){
            ddq.add("Autor", filtros[0]);
        }
        if(filtros[1] != null){
            ddq.add("Año", filtros[1]);
        }
        if(filtros[2] != null){
            ddq.add("AuthorKeywords", filtros[2]);
        }
        if(filtros[3] != null){
            ddq.add("IndexKeywords", filtros[3]);
        }
        DrillSideways ds = new DrillSideways(searcher, fconfig, taxoReader);
        DrillSidewaysResult dsresult = ds.search(ddq, 100);
        
        return dsresult;
        
    }
    
    public Facets get_facetas_drill() throws IOException{
        
        FacetsCollector.search(searcher, ddq, 20, fc);
        Facets facetas = new FastTaxonomyFacetCounts(taxoReader,fconfig,fc);
        return facetas;
    }

}
