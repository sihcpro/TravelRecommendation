package carskit.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import librec.util.Logs;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rodrigo
 */
public class CARSGraph extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int width = 800;
    private int height = 600;
    private int padding = 25;
    private int labelPadding = 25;
//    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 4;
    private int numberYDivisions = 10;
    
    private static Map<String, Color> map_colors = new HashMap<>();

    private Map<String, List<Double>> map_result;

    public CARSGraph(List<List<Double>> values, List<String> algos) {
        CARSResult re = new CARSResult(algos, values);
        re.make_map();
        map_result = re.map_result;
    }
    
    public CARSGraph(Map<String, List<Double>> map) {
    	map_result = map;
    }
    
    private void init_color() {
    	map_colors.put("GlobalAvg", new Color(0, 0, 0, 255));//
        map_colors.put("UserAvg", new Color(0, 0, 205, 255));//
        map_colors.put("ItemAvg", new Color(0, 100, 0, 255));//
        map_colors.put("ContextAvg", new Color(255, 0, 0, 255));//
        map_colors.put("UserItemAvg", new Color(50, 205, 50, 255));//
        map_colors.put("UserContextAvg", new Color(255, 160, 122, 255));//
        map_colors.put("ItemContextAvg", new Color(255, 215, 0, 255));//
        map_colors.put("", new Color(255, 165, 0, 255));//
        map_colors.put("UserSplitting", new Color(255, 140, 0, 255));//
        map_colors.put("UISplitting", new Color(189, 183, 107, 255));//
        map_colors.put("ItemSplitting", new Color(255, 218, 185, 255));//
        map_colors.put("CPTF", new Color(135, 206, 235, 255));//
        map_colors.put("CAMF_MCS", new Color(128, 128, 0, 255));//
        map_colors.put("CAMF_LCS", new Color(173, 255, 47, 255));//
        map_colors.put("CAMF_CUCI", new Color(127, 255, 212, 255));//
        map_colors.put("CAMF_CU", new Color(32, 178, 170, 255));//
        map_colors.put("CAMF_CI", new Color(0, 128, 128, 255));//
        map_colors.put("CAMF_C", new Color(72, 61, 139, 255));//
        map_colors.put("GCSLIM_CC", new Color(30, 144, 255, 255));//
        map_colors.put("CSLIM_CUCI", new Color(0, 0, 128, 255));//
        map_colors.put("CAMF_ICS", new Color(139, 0, 0, 255));//
        map_colors.put("CSLIM_ICS", new Color(128, 0, 128, 255));//
        map_colors.put("CSLIM_LCS", new Color(138, 43, 226, 255));//
        map_colors.put("CSLIM_MCS", new Color(238, 130, 238, 255));//
        map_colors.put("GCSLIM_ICS", new Color(255, 20, 147, 255));//
        map_colors.put("GCSLIM_LCS", new Color(199, 21, 133, 255));//
        map_colors.put("GCSLIM_MCS", new Color(192, 192, 192, 255));//
        map_colors.put("CSLIM_CI", new Color(112, 128, 144, 255));//
        map_colors.put("CSLIM_CU", new Color(165, 42, 42, 255));//
        map_colors.put("CSLIM_C", new Color(255, 222, 173, 255));//
        map_colors.put("FM", new Color(44, 200, 230, 180));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (map_colors.isEmpty())
            init_color();

        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            g2.setColor(gridColor);
            g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
            g2.setColor(Color.BLACK);
            String yLabel = ((int) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(yLabel);
            g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            g2.drawLine(x0, y0, x1, y1);
        }

        // create x and y axes 
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        
        for (Map.Entry<String, List<Double>> result:map_result.entrySet()) {
        	if (!map_colors.containsKey(result.getKey())) {
            	Logs.debug("Alog {} don't have color!", result.getKey());
        		continue;
        	} else {
            	Logs.debug("Paint {}", result.getKey());
        	}
        	
	        List<Double> scores = result.getValue();
	        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores.size() - 1);
	        double yScale = ((double) getHeight() - (2 * padding) - labelPadding) / (getMaxScore() - getMinScore());
	
	        List<Point> graphPoints = new ArrayList<>();
	        for (int i = 0; i < scores.size(); i++) {
	            int x1 = (int) (i * xScale + padding + labelPadding);
	            int y1 = (int) ((getMaxScore() - scores.get(i)) * yScale + padding);
	            graphPoints.add(new Point(x1, y1));
	        }	

	        Stroke oldStroke = g2.getStroke();
	        g2.setColor(map_colors.get(result.getKey()));
	        g2.setStroke(GRAPH_STROKE);
	        for (int i = 0; i < graphPoints.size() - 1; i++) {
	            int x1 = graphPoints.get(i).x;
	            int y1 = graphPoints.get(i).y;
	            int x2 = graphPoints.get(i + 1).x;
	            int y2 = graphPoints.get(i + 1).y;
	            g2.drawLine(x1, y1, x2, y2);
	        }
	
	        g2.setStroke(oldStroke);
	        g2.setColor(pointColor);
	        for (int i = 0; i < graphPoints.size(); i++) {
	            int x = graphPoints.get(i).x - pointWidth / 2;
	            int y = graphPoints.get(i).y - pointWidth / 2;
	            int ovalW = pointWidth;
	            int ovalH = pointWidth;
	            g2.fillOval(x, y, ovalW, ovalH);
	        }
        }

        
        // and for x axis
        String[] a = {"Pre5", "Pre10", "MAP5", "Map10", "Rec5", "Rec10", "NDCG5", "NDCG10", "AUC5", "AUC10", "MRR5", "MRR10"};
        Vector<String> title_x = new Vector<String>(Arrays.asList(a));
        for (int i = 0; i < title_x.size(); i++) {
//            if (scores.size() > 1) {
                int x0 = i * (getWidth() - padding * 2 - labelPadding) / (title_x.size() - 1) + padding + labelPadding;
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                if ((i % ((int) ((title_x.size() / 20.0)) + 1)) == 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth(title_x.get(i));
                    g2.drawString(title_x.get(i), x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
//            }
        }
    }

//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(width, height);
//    }

    private double getMinScore() {
//        double minScore = Double.MAX_VALUE;
//        for (Double score : scores) {
//            minScore = Math.min(minScore, score);
//        }
        return 0;
    }

    private double getMaxScore() {
//        double maxScore = Double.MIN_VALUE;
//        for (Double score : scores) {
//            maxScore = Math.max(maxScore, score);
//        }
        return 1;
    }

//    public void setScores(List<List<Double>> values) {
//        this.result_values = values;
//        invalidate();
//        this.repaint();
//    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setSize(int Width, int Height) {
        width = Width;
        height = Height;
    }
    
    private static void createAndShowGui() {
        List<Double> scores = new ArrayList<>();
        Random random = new Random();
        int maxDataPoints = 12;
        int maxScore = 1;
        for (int i = 0; i < maxDataPoints; i++) {
            scores.add((double) random.nextDouble() * maxScore);
        }
        List<Double> scores2 = new ArrayList<>();
        for (int i = 0; i < maxDataPoints; i++) {
            scores2.add((double) random.nextDouble() * maxScore);
        }

        List<List<Double>> matrix = new ArrayList<>();
        matrix.add(scores);
        matrix.add(scores2);
        
        
        List<String> result_algos = new ArrayList<String>();
        result_algos.add("CAMF_C");
        result_algos.add("CAMF_CUCI");
        
        CARSGraph mainPanel = new CARSGraph(matrix, result_algos);
        mainPanel.setPreferredSize(new Dimension(mainPanel.getWidth(), mainPanel.getHeight()));
        JFrame frame = new JFrame("DrawGraph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            createAndShowGui();
         }
      });
   }
}