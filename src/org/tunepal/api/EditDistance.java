/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tunepal.api;

import java.util.Collection;
import java.util.Arrays;

/**
 *
 * @author Bryan Duggan
 */
public class EditDistance
{

    private static int Minimum(int a, int b, int c)
    {
        int mi;

        mi = a;
        if (b < mi)
        {
            mi = b;
        }
        if (c < mi)
        {
            mi = c;
        }
        return mi;

    }

    public static float LD(String s, String t)
    {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1

        n = s.length();
        m = t.length();
        if (n == 0)
        {
            return m;
        }
        if (m == 0)
        {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2

        for (i = 0; i <= n; i++)
        {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++)
        {
            d[0][j] = j;
        }

        // Step 3

        for (i = 1; i <= n; i++)
        {

            s_i = s.charAt(i - 1);

            // Step 4

            for (j = 1; j <= m; j++)
            {

                t_j = t.charAt(j - 1);

                // Step 5

                if (s_i == t_j)
                {
                    cost = 0;
                }
                else
                {
                    cost = 1;
                }

                // Step 6

                d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);

            }

        }
        
         
          for (int ii = 0  ; ii < d.length ; ii ++)
        {
            for (int jj = 0  ; jj < d[ii].length; jj ++)
            {
                System.out.print(d[ii][jj] + "\t");
            }
            System.out.println();
        }
         
        // Step 7
    /*
        float max;
        float min;
        float ed;
        if (s.length() > t.length())
        {
        max = s.length();
        min = t.length();
        }
        else
        {
        max = t.length();
        min = s.length();
        }
         */
        float ed = d[n][m];
        return ed;

    }

    public static int getLevenshteinDistance(String s, String t)
    {
        if (s == null || t == null)
        {
            throw new IllegalArgumentException("Strings must not be null");
        }

        /*
        The difference between this impl. and the previous is that, rather 
        than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
        we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
        is the 'current working' distance array that maintains the newest distance cost
        counts as we iterate through the characters of String s.  Each time we increment
        the index of String t we are comparing, d is copied to p, the second int[].  Doing so
        allows us to retain the previous cost counts as required by the algorithm (taking 
        the minimum of the cost count to the left, up one, and diagonally up and to the left
        of the current cost count being calculated).  (Note that the arrays aren't really 
        copied anymore, just switched...this is clearly much better than cloning an array 
        or doing a System.arraycopy() each time  through the outer loop.)
        Effectively, the difference between the two implementations is this one does not 
        cause an out of memory condition when calculating the LD over two very large strings.  		
         */

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0)
        {
            return m;
        }
        else if (m == 0)
        {
            return n;
        }

        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++)
        {
            p[i] = i;
        }

        for (j = 1; j <= m; j++)
        {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++)
            {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now 
        // actually has the most recent cost counts
        return p[n];
    }

    public static float editDistance(String feature1, String feature2)
    {
        int length_1 = feature1.toString().length();
        int length_2 = feature2.toString().length();
        int difference = 0;

        char sc;

        if (length_1 == 0)
        {
            return length_1;
        }
        if (length_2 == 0)
        {
            return length_2;
        }

        int[][] d = new int[length_1 + 1][length_2 + 1];

        for (int i = 1; i <= length_1; i++)
        {
            sc = feature1.toString().charAt(i - 1);
            for (int j = 1; j <= length_2; j++)
            {
                int v = d[i - 1][j - 1];
                if (feature2.toString().charAt(j - 1) != sc)
                {
                    difference = 1;
                }
                else
                {
                    difference = 0;
                }
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), v + difference);
            }
        }

        float ed = d[length_1][length_2];
        return ed;
    }

    public static int[] edSubString(String pattern, String text, int[][] d)
    {
        int pLength = pattern.length();
        int tLength = text.length();
        int difference = 0;

        char sc;

        if (pLength == 0)
        {
            return null;
        }
        if (tLength == 0)
        {
            return null;
        }

        
        // Initialise the first row
        for (int i = 0; i < tLength + 1; i++)
        {
            d[0][i] = 0;
        }
        // Now make the first col = 0,1,2,3,4,5,6
        for (int i = 0; i < pLength + 1; i++)
        {
            d[i][0] = i;
        }


        for (int i = 1; i <= pLength; i++)
        {
            sc = pattern.charAt(i - 1);
            for (int j = 1; j <= tLength; j++)
            {
                int v = d[i - 1][j - 1];
                //if ((text.charAt(j - 1) != sc) && (text.charAt(j - 1) != 'Z') && sc != 'Z')                
                if ((text.charAt(j - 1) != sc)  && sc != 'Z')
                {
                    difference = 1;
                }
                else
                {
                    difference = 0;
                }
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), v + difference);
            }
        }
        
         /*
          for (int i = 0 ; i < d.length ; i ++)
        {
            for (int j = 0 ; j < d[i].length; j ++)
            {
                System.out.print(d[i][j] + "\t");
            }
            System.out.println();
        }
         */
        return d[pLength];
    }
    
    public static float minEdSemex(int[] pattern, int[] text, int[][] d)
    {
        int pLength = pattern.length;
        int tLength = text.length;
        int difference = 0;

        int sc;

        if (pLength == 0)
        {
            return -1;
        }
        if (tLength == 0)
        {
            return -1;
        }

        // int[][] d = new int[pLength + 1][tLength + 1];

        // Initialise the first row
        for (int i = 0; i < tLength + 1; i++)
        {
            d[0][i] = 0;
        }
        // Now make the first col = 1,2,3,4,5,6
        for (int i = 0; i < pLength + 1; i++)
        {
            d[i][0] = i;
        }


        for (int i = 1; i <= pLength; i++)
        {
            sc = pattern[i - 1];
            for (int j = 1; j <= tLength; j++)
            {
                int v = d[i - 1][j - 1];
                if (j - 2 < 0 || i - 2 < 0)
                {
                    difference = 0;
                }
                else if ((text[j - 1] - text[j - 2]) != (pattern[i - 1] - pattern[i - 2]))
                // if (text[j - 1] != sc) 
                {
                    difference = 1;
                }
                else
                {
                    difference = 0;
                }
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), v + difference);
            }
        }
        
      
        
        
        /* for (int i = 0 ; i < d.length ; i ++)
        {
            for (int j = 0 ; j < d[i].length; j ++)
            {
                System.out.print(d[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
        */
        int[] lastRow = d[pLength];
        int min = Integer.MAX_VALUE;
        for (int i = 1; i < tLength + 1; i++)
        {
            int c = lastRow[i];
            // System.out.println(c);
            if (c < min)
            {
                min = c;
            }
        }
        return min;

    }
    
    
    public static float minEdSubString(String pattern, String text, int[][] d)
    {
        int[] lastRow = edSubString(pattern, text, d);
        int min = Integer.MAX_VALUE;
        int tLength = text.length();
        for (int i = 0; i < tLength + 1; i++)
        {
            int c = lastRow[i];
            // System.out.println(c);
            if (c < min)
            {
                min = c;
            }
        }
        float ed = min;
        return ed;
    }

    public static void main(String[] args)
    {
    	// Pasucais
        int[] Pasucais = {71,69,71,74,76,78,81,83,81,78,78,78,78,76,78,76,74,71,71,69,71,74,71,69,74,69,71,74,76,78,81,83,81,78,78,78,78,76,78,76,74,71,71,69,71,74,74,74,71,69,71,74,76,78,81,83,81,78,78,78,78,76,78,76,74,71,71,69,71,74,71,69,74,69,71,74,76,78,81,83,81,78,78,78,78,76,78,76,74,71,71,69,71,74,74,74,74,74,73,71,71,71,71,74,71,69,69,69,74,73,74,76,76,76,76,78,79,78,76,74,74,74,73,71,71,71,71,74,71,69,69,69,74,73,74,76,76,76,76,78,76,74,74,74,74,74,73,71,71,71,71,74,71,69,69,69,74,73,74,76,76,76,76,78,79,78,76,74,74,74,73,71,71,71,71,74,71,69,69,69,74,73,74,76,76,76,76,78,76,74,74,74,69,71,74,69,71,74,69,71,74,76,76,76,76,73,69,76,76,76,76,78,79,78,76,74,69,71,74,69,71,74,69,71,74,76,76,76,76,73,69,76,76,76,76,78,76,74,74,74,69,71,74,69,71,74,69,71,74,76,76,76,76,73,69,76,76,76,76,78,79,78,76,74,69,71,74,69,71,74,69,71,74,76,76,76,76,73,69,76,76,76,76,78,76,74,74,74};
        
        
        // Tonres (Matched)
        int[] Tonres = {64,66,66,66,69,66,64,64,62,71,69,71,62,66,64,66,67,66,67,69,71,66,69,66,64,66,66,66,69,66,64,64,62,71,69,71,62,66,64,66,67,66,67,69,66,62,62,62,64,66,66,66,69,66,64,64,62,71,69,71,62,66,64,66,67,66,67,69,71,66,69,66,64,66,66,66,69,66,64,64,62,71,69,71,62,66,64,66,67,66,67,69,66,62,62,62,76,78,78,74,74,73,74,76,74,73,71,73,74,78,78,74,74,73,74,69,71,66,69,74,76,78,78,74,74,73,74,76,74,73,71,73,74,78,81,78,79,76,73,74,78,76,74,74,76,78,78,74,74,73,74,76,74,73,71,73,74,78,78,74,74,73,74,69,71,66,69,74,76,78,78,74,74,73,74,76,74,73,71,73,74,78,81,78,79,76,73,74,78,76,74,74};
        
        // Tonres (Unmatched)
        int[] Tonres1 = {66,66,66,69,67,64,64,62,71,69,71,62,66,66,66,67,67,67,69,71,66,69,66,64,66,66,66,69,67,64,64,62,71,69,71,62,66,66,66,67,66,67,71,69,66,62,62,62,66,66,66,69,67,64,64,62,71,69,71,62,66,66,66,67,67,67,69,71,66,69,66,64,66,66,66,69,67,64,64,62,71,69,71,62,66,66,66,67,66,67,71,69,66,62,62,62,78,78,74,74,73,74,76,74,73,71,73,74,78,78,74,74,73,74,69,71,66,69,74,76,78,78,74,74,73,74,76,74,73,71,73,74,78,78,78,79,76,73,76,74,73,74,74,74,78,78,74,74,73,74,76,74,73,71,73,74,78,78,74,74,73,74,69,71,66,69,74,76,78,78,74,74,73,74,76,74,73,71,73,74,78,78,78,79,76,73,76,74,73,74,74,74};                
        int[] pattern = {78,78,78,81,78,76,76,74,83,81,82,74,78,78,78,79,79,79,81,29,81,81,78,76,78,78,78,81,78,76,76,74,83,81,82,74,78,78,78,79,78,79,83,81,78,74,29,81,78,78,78,81,78,76,76,74,83,81,79,78,78,78,79,79,79,81};
        
        int[] fairwell = {69,69,69,60,64,64,64,66,67,64,62,71,67,71,62,71,69,69,69,60,64,64,64,66,67,64,62,66,64,69,69,67,69,69,69,60,64,64,64,66,67,64,62,71,62,62,79,79,76,74,76,79,81,81,83,81,79,76,74,71,71,69,69,69,69,69,69,60,64,64,64,66,67,64,62,71,67,71,62,71,69,69,69,60,64,64,64,66,67,64,62,66,64,69,69,67,69,69,69,60,64,64,64,66,67,64,62,71,62,62,79,79,76,74,76,79,81,81,83,81,79,76,74,71,71,69,69,69,81,81,81,79,81,79,76,78,79,79,79,81,79,76,74,76,81,81,81,79,81,79,76,78,79,76,74,71,71,69,69,69,81,79,76,78,79,81,79,76,74,74,74,71,67,69,71,74,73,69,76,69,71,74,76,78,79,76,74,71,71,69,69,69,81,81,81,79,81,79,76,78,79,79,79,81,79,76,74,76,81,81,81,79,81,79,76,78,79,76,74,71,71,69,69,69,81,79,76,78,79,81,79,76,74,74,74,71,67,69,71,74,73,69,76,69,71,74,76,78,79,76,74,71,71,69,69,69,76,69,71,69,76,69,71,69,74,74,74,71,67,69,71,74,76,69,71,69,76,69,69,81,79,76,74,71,71,69,69,69,76,69,71,69,76,69,71,69,74,74,74,71,67,69,71,74,73,73,71,69,71,74,76,78,79,76,74,71,71,69,69,69,76,69,71,69,76,69,71,69,74,74,74,71,67,69,71,74,76,69,71,69,76,69,69,81,79,76,74,71,71,69,69,69,76,69,71,69,76,69,71,69,74,74,74,71,67,69,71,74,73,73,71,69,71,74,76,78,79,76,74,71,71,69,69,69,81,76,73,76,81,76,73,76,79,74,71,74,79,74,71,74,81,76,73,76,81,81,81,78,79,76,74,71,71,69,69,69,81,79,76,78,79,79,79,76,74,76,74,71,67,69,71,71,73,69,76,69,74,74,76,78,79,76,74,71,71,69,69,69,81,76,73,76,81,76,73,76,79,74,71,74,79,74,71,74,81,76,73,76,81,81,81,78,79,76,74,71,71,69,69,69,81,79,76,78,79,79,79,76,74,76,74,71,67,69,71,71,73,69,76,69,74,74,76,78,79,76,74,71,71,69,69,69};
        int[] patternCurragh = {79,79,83,86,79,83,86,88,81,83,81,88,81,83,81,86,83,79,81,83,81,81,81,88,81,83,81,88,81,83,81,79,79,79,83,86,83,86,84,84,84,81,83,83,91,88,86,83,79,81,83,81,81,81,88,81,83,81,88,81};
    	int curragh[] = {76,69,72,69,76,69,72,69,67,67,71,67,74,67,71,67,76,69,72,69,76,69,72,69,67,69,71,74,72,69,69,69,76,69,72,69,76,69,72,69,67,67,71,67,74,67,69,71,72,72,72,69,71,71,71,72,74,71,67,69,71,69,69,69,76,69,72,69,76,69,72,69,67,67,71,67,74,67,71,67,76,69,72,69,76,69,72,69,67,69,71,74,72,69,69,69,76,69,72,69,76,69,72,69,67,67,71,67,74,67,69,71,72,72,72,69,71,71,71,72,74,71,67,69,71,69,69,69,72,72,76,72,79,72,76,72,72,72,76,72,74,71,67,71,72,72,76,72,79,72,76,72,74,71,67,69,71,69,69,71,72,72,76,72,79,72,76,72,72,72,76,72,74,71,67,71,72,72,72,69,71,71,79,76,74,71,67,69,71,69,69,69,72,72,76,72,79,72,76,72,72,72,76,72,74,71,67,71,72,72,76,72,79,72,76,72,74,71,67,69,71,69,69,71,72,72,76,72,79,72,76,72,72,72,76,72,74,71,67,71,72,72,72,69,71,71,79,76,74,71,67,69,71,69,69,69};
    	int[][] d = new int[1000][1000];
        
        System.out.println("Transposition invariant ED: " + minEdSemex(pattern, Tonres, d));
        System.out.println("Transposition invariant ED: " + minEdSemex(patternCurragh, curragh, d));
        System.out.println("Transposition invariant ED: " + minEdSemex(patternCurragh, fairwell, d));
                
        

        /*
         String toFind, searchIn;

        
         toFind = "DFGDGBDEGGAB";
        searchIn = "DGGGDGBDEFGAB"; // DGGGDGBDEFGAB"; //";


        System.out.println("beditDistance: " + edSubString(toFind, searchIn));
        System.out.println("Min editDistance substring: " + minEdSubString(toFind, searchIn));
        System.out.println("editDistance: " + editDistance(toFind, searchIn));
        System.out.println("LD: " + LD(toFind, searchIn));
        System.out.println("getLevenshteinDistance: " + getLevenshteinDistance(toFind, searchIn));

        float ed = getLevenshteinDistance(toFind, searchIn);
        ed = 1.0f - ((searchIn.length() - ed) / toFind.length());

        System.out.println("Normalised ed " + ed);
         */
    }
}
