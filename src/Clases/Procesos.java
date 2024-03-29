
package Clases;

import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Procesos {
        
    private final double LIGHT_SPEED = 30000000000d;
    
    private double initIntensity;
    private double distance;
    private double mirror1;
    private double mirror2;
    private double refractive;
    private double coefficient;
    
    private double amplitudeTransmittance;
    private double amplitudeReflectance;
    private double transmitanceFinesse;
    private double maxTransmittance;
    private double lineWidth;   
    
    private double roundTripAt;
    private double finesse;
    private double iMax;
    private double fSpacing;
    private double iMin;
    private double ar;
    private double photonLife;
    
    //{q, v, Factor Q }
    private String[][] resonatorModes = new String[20][3]; 
    
    //Intensity vs Frequency table
    //{frequency, intensity, transmitance}
    private double[][] intensityTransmitanceFrequencyTable = new double[100][3]; 

    public Procesos(double intensity, double distance, double mirror1, double mirror2, double refractive, double coefficient) {
        this.initIntensity = intensity;
        this.distance = distance;
        this.mirror1 = mirror1 / 100;
        this.mirror2 = mirror2 / 100;
        this.refractive = refractive;
        this.coefficient = coefficient;
        resonador();
    }
    
    private void resonador(){
//        round-trip amplitude attenuation
        roundTripAt = Math.sqrt(mirror1 * mirror2 * Math.exp(-2 * coefficient * distance));
        
//        Finesse
        finesse = Math.PI * Math.sqrt(roundTripAt) / (1 - roundTripAt);
        
//        max intensity
        iMax = initIntensity / Math.pow((1 - roundTripAt), 2);
        
//        Frequency spacing of adjacentresonator modes
        fSpacing = (LIGHT_SPEED / refractive) / (2 * distance);
        
//        minumum intensity
        iMin = iMax / (1 + Math.pow((2 * finesse / Math.PI), 2));
        
//        distribuited-loss coefficient
        ar = coefficient + (1 / 2 * distance) * Math.log(1 / mirror1 * mirror2);
        
//        Photon lifetime
        photonLife = 1 / (LIGHT_SPEED * ar);
        
//        t = t1 * t2        
        amplitudeTransmittance = (1 - mirror1) * (1 - mirror2);
        
        lineWidth = (fSpacing / 1000000000) / finesse;
        
        amplitudeReflectance = mirror1 * mirror2;
        
        transmitanceFinesse = Math.PI * Math.sqrt(amplitudeReflectance) / (1 - amplitudeReflectance);
        
        maxTransmittance = Math.pow(amplitudeTransmittance, 2) / Math.pow(1 - amplitudeReflectance, 2);
        
        for (int i = 0; i < 20; i++) {
            resonatorModes[i][0] = (i + 1) + ""; // mode number
            resonatorModes[i][1] = (Double.parseDouble(resonatorModes[i][0]) * fSpacing / 1000000000) + ""; //resonance frecuency
            resonatorModes[i][2] = (round((2 * Math.PI * Double.parseDouble(resonatorModes[i][1])  * 1000000000 * photonLife))) + "";
        }
        
        intensityTransmitanceFrequencyTable[0][0] = fSpacing / 5; //frequency
        intensityTransmitanceFrequencyTable[0][1] = iMax / (1 + Math.pow(2 * finesse / Math.PI, 2) * Math.pow(Math.sin(Math.PI * intensityTransmitanceFrequencyTable[0][0] / fSpacing), 2)); //Intensity
        intensityTransmitanceFrequencyTable[0][2] = maxTransmittance / (1 + Math.pow(2 * transmitanceFinesse / Math.PI, 2) * Math.pow(Math.sin(Math.PI * intensityTransmitanceFrequencyTable[0][0] / fSpacing), 2));//Transmitance
        
        for (int i = 1; i < 100; i++) {
            intensityTransmitanceFrequencyTable[i][0] = intensityTransmitanceFrequencyTable[i -1][0] + intensityTransmitanceFrequencyTable[0][0]; //frequency
            intensityTransmitanceFrequencyTable[i][1] = iMax / (1 + Math.pow(2 * finesse / Math.PI, 2) * Math.pow(Math.sin(Math.PI * intensityTransmitanceFrequencyTable[i][0] / fSpacing), 2)); //Intensity
            intensityTransmitanceFrequencyTable[i][2] = maxTransmittance / (1 + Math.pow(2 * transmitanceFinesse / Math.PI, 2) * Math.pow(Math.sin(Math.PI * intensityTransmitanceFrequencyTable[i][0] / fSpacing), 2));;//Transmitance
        }        
        
    }
    
    public void crearTabla(JTable tabla){        
        javax.swing.table.DefaultTableModel model = new DefaultTableModel(resonatorModes, new String[]{"Mode Number (q)", "Resonance Frecuency (GHz)", "Q Factor"});
        tabla.setModel(model);
    }
    
    public void graficar(JPanel grafica){
        grafica.removeAll();
        XYSeriesCollection data = new XYSeriesCollection();
        XYSeries series = new XYSeries("Internal Intensity");
        for (int i = 0; i < intensityTransmitanceFrequencyTable.length; i++) {
            series.add(intensityTransmitanceFrequencyTable[i][0]/1000000000, intensityTransmitanceFrequencyTable[i][1]);
        }
        data.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart("Resonance", "Frecuency (GHz)", "Intensity (mW/cm^2)", data , PlotOrientation.VERTICAL, true, true, false);
        ChartPanel cp = new ChartPanel(chart);
        grafica.setLayout(new java.awt.BorderLayout());
        grafica.add(cp);
    } 

    public String getFinesse() {
        return round(finesse)+"";
    }

    public String getAr() {
        return ar + " cm^-1";
    }

    public String getfSpacing() {
        return round((fSpacing / 1000000000))+" GHz";
    }

    public String getiMax() {
        return round(iMax)+" mW/cm^2";
    }    
    
    public String getPhotonLifeTime(){
        return photonLife + " ps";
    }

    public String getLineWidth() {
        return round(lineWidth) + " GHz";
    }    
    
    
    private double round(double x){
        double temp = x * 100d;
        temp = Math.round(temp);
        temp = temp / 100d;
        return temp;
    }

    
    
    
    
}
