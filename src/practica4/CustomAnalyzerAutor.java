package practica4;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;

public class CustomAnalyzerAutor extends Analyzer{
    
    @Override
    protected TokenStreamComponents createComponents(String string){
        Tokenizer tokenizer = new CustomTokenizerAutor();
        TokenStream tokenstream = tokenizer;
        // Pasamos a minusculas
        tokenstream = new LowerCaseFilter(tokenstream);
        // Eliminamos espacios finales e iniciales
        tokenstream = new TrimFilter(tokenstream);
        return new TokenStreamComponents(tokenizer, tokenstream);

    }
}