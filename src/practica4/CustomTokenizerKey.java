package practica4;
import org.apache.lucene.analysis.util.CharTokenizer;


public class CustomTokenizerKey extends CharTokenizer{

    @Override
    protected boolean isTokenChar(int c) {
        /// Compara si es un espacio, una letra o un numero
        char[] c1 = Character.toChars(c);
        if(Character.compare(c1[0], ';') != 0) {
            
            return true;
        }
        else {
            return false;
        }

    }
}