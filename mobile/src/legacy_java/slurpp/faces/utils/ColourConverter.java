package slurpp.faces.utils;

import android.graphics.Color;

/**
 * Created by kylemontague on 17/09/15.
 */
public class ColourConverter {

    public static double[] RGBtoLab(int rgb){
        return XYZtoLAB(RGBtoXYZ(rgb));
    }

    public static double[] RGBtoXYZ(int rgb){
        double var_R = (Color.red(rgb) / 255.0);       //R from 0 to 255
        double var_G = (Color.green(rgb) / 255.0);        //G from 0 to 255
        double var_B = (Color.blue(rgb) / 255.0);        //B from 0 to 255

        if ( var_R > 0.04045 )
            var_R = Math.pow((( var_R + 0.055 ) / 1.055 ),2.4);
        else
            var_R = var_R / 12.92;

        if ( var_G > 0.04045 )
            var_G = Math.pow((( var_G + 0.055 ) / 1.055 ),2.4);
        else
            var_G = var_G / 12.92;

        if ( var_B > 0.04045 )
            var_B = Math.pow((( var_B + 0.055 ) / 1.055 ),2.4);
        else
            var_B = var_B / 12.92;

        var_R = var_R * 100;
        var_G = var_G * 100;
        var_B = var_B * 100;

        //Observer. = 2°, Illuminant = D65
        double[] xyz = new double[3];
        xyz[0] = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
        xyz[1] = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
        xyz[2] = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;
        return xyz;
    }

    //Observer= 2°, Illuminant= D65
    static double ref_X =  95.047;
    static double ref_Y =  100.000;
    static double ref_Z =  108.883;
    public static double[] XYZtoLAB(double[] xyz){
        double var_X = xyz[0]/ ref_X;         //ref_X =  95.047   Observer= 2°, Illuminant= D65
        double var_Y = xyz[1] / ref_Y;          //ref_Y = 100.000
        double var_Z = xyz[2] / ref_Z;          //ref_Z = 108.883

        if ( var_X > 0.008856 )
            var_X = Math.pow(var_X,( 1/3 ));
        else
            var_X = ( 7.787 * var_X ) + ( 16 / 116 );

        if ( var_Y > 0.008856 )
            var_Y = Math.pow(var_Y,( 1/3 ));
        else
            var_Y = ( 7.787 * var_Y ) + ( 16 / 116 );
        if ( var_Z > 0.008856 )
            var_Z = Math.pow(var_Z,( 1/3 ));
        else
            var_Z = ( 7.787 * var_Z ) + ( 16 / 116 );

        double[] lab = new double[3];
        lab[0] = ( 116 * var_Y ) - 16;
        lab[1] = 500 * ( var_X - var_Y );
        lab[2] = 200 * ( var_Y - var_Z );
        return lab;
    }


    public static double[] rgb2lab(int rgb) {
        //http://www.brucelindbloom.com

        int R = Color.red(rgb);
        int G = Color.green(rgb);
        int B = Color.blue(rgb);
        double[] lab = new double[3];
        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f/24389.f;
        float k = 24389.f/27.f;

        float Xr = 0.964221f;  // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = R/255.f; //R 0..1
        g = G/255.f; //G 0..1
        b = B/255.f; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r/12;
        else
            r = (float) Math.pow((r+0.055)/1.055,2.4);

        if (g <= 0.04045)
            g = g/12;
        else
            g = (float) Math.pow((g+0.055)/1.055,2.4);

        if (b <= 0.04045)
            b = b/12;
        else
            b = (float) Math.pow((b+0.055)/1.055,2.4);


        X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
        Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
        Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;

        // XYZ to Lab
        xr = X/Xr;
        yr = Y/Yr;
        zr = Z/Zr;

        if ( xr > eps )
            fx =  (float) Math.pow(xr, 1/3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if ( yr > eps )
            fy =  (float) Math.pow(yr, 1/3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if ( zr > eps )
            fz =  (float) Math.pow(zr, 1/3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = ( 116 * fy ) - 16;
        as = 500*(fx-fy);
        bs = 200*(fy-fz);

        lab[0] = (Ls + .5);
        lab[1] = (as + .5);
        lab[2] = (bs + .5);

        return lab;
    }
}
