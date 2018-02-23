package slurpp.faces_lib.templates;

import com.manolovn.trianglify.generator.color.ColorGenerator;

/**
 * Created by Kyle Montague on 21/08/2017.
 */

public class PaletteColorGenerator implements ColorGenerator {

    int count = 0;
    int[] colors;
//    Random random = new Random(System.currentTimeMillis());
    int current_index = 0;


    public PaletteColorGenerator(int[] colors) {
        this.colors = colors;
        setCount(colors.length);
    }

    @Override
    public int nextColor() {
        if(count > 0) {
            current_index = (current_index + 1) % colors.length;
            return colors[current_index];
        }
        return -1;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }
}
