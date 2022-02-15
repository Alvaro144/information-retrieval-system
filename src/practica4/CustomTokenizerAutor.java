package practica4;
import org.apache.lucene.analysis.util.CharTokenizer;


public class CustomTokenizerAutor extends CharTokenizer{
    
    @Override
    protected boolean isTokenChar(int caracter) {
        // Compara si es un espacio, una letra o un numero
        char[] c1 = Character.toChars(caracter);

        //Si es distinto de ,, devuelve verdadero
        if(Character.compare(c1[0], ' ') != 0) {
            return true;
        }
        else {
            if(Character.compare(c1[0], ',') == 0){
                return false;
            }else{
                return false;
            }
        }

    }

}