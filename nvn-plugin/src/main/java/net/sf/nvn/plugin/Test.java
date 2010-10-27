package net.sf.nvn.plugin;

public class Test
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        long tm = 10485760;
        long fsizeB = 8;
        
        double chunks = Math.ceil((double)fsizeB / (double)tm);
        System.out.println(chunks);
    }

}
