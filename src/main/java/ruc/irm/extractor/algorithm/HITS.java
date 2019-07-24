package ruc.irm.extractor.algorithm;

import java.lang.Math;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Comparator;
import java.util.Arrays;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

import java.io.IOException;

/**
 *  This class comes from: https://github.com/kish1/HITS/blob/master/HITS.java
 */
class Node
{
    private Integer id;
    private Double auth;
    private Double hubs;
    private HashSet<Integer> inlinks;
    private HashSet<Integer> outlinks;

    public Node()
    {
        this.id = null;
        this.auth = null;
        this.hubs = null;
        this.inlinks = null;
        this.outlinks = null;
    }
    public void initialize(Integer id, HashSet<Integer> inlinks, HashSet<Integer> outlinks)
    {
        this.id = id;
        this.inlinks = inlinks;
        this.outlinks = outlinks;
        this.auth = new Double(1.0f);
        this.hubs = new Double(1.0f);
    }

    public void computeAuth(HashMap<Integer, Node> nodesMap)
    {
        int link_id;
        double score = 0.0f;
        for(Iterator<Integer> i = this.inlinks.iterator(); i.hasNext();)
        {
            link_id = i.next();
            Node node = nodesMap.get(link_id);
            score += node.getHubs();
        }
        this.auth = score;
    }

    public void computeHubs(HashMap<Integer, Node> nodesMap)
    {
        int link_id;
        double score = 0.0f;
        for(Iterator<Integer> i = this.outlinks.iterator(); i.hasNext();)
        {
            link_id = i.next();
            Node node = nodesMap.get(link_id);
            score += node.getAuth();
        }
        this.hubs = score;
    }

    public void normalizeAuth(Double factor)
    {
        this.auth = this.auth/factor;
    }

    public void normalizeHubs(Double factor)
    {
        this.hubs = this.hubs/factor;
    }

    public Double getAuth()
    {
        return this.auth;
    }

    public Double getHubs()
    {
        return this.hubs;
    }

    public Integer getId()
    {
        return this.id;
    }
}

class Graph
{
    private HashMap<Integer, Node> nodesMap;
    private Node[] nodes;
    private int size;
    private double[] auths;
    private double[] hubs;
    private double[] previous_auths;
    private double[] previous_hubs;
    private double epsilon;

    public Graph(int size)
    {
        this.size = size;
        this.nodes = new Node[size];
        this.nodesMap = null;
        this.auths = null;
        this.hubs = null;
        this.previous_auths = null;
        this.previous_hubs = null;
        this.epsilon = 0.00001f;
    }

    public void initialize(HashSet<Integer> base, HashMap<Integer, HashSet<Integer>> inlinks, HashMap<Integer, HashSet<Integer>> outlinks)
    {
        int j = 0;
        int link_id;
        Node node;
        this.nodesMap = new HashMap<Integer, Node>();
        for(Iterator<Integer> i = base.iterator(); i.hasNext();)
        {
            link_id = i.next();
            node = new Node();
            node.initialize(link_id, inlinks.get(link_id), outlinks.get(link_id));
            this.nodesMap.put(link_id, node);
            this.nodes[j++] = node;
        }
    }

    public void iterate()
    {
        double auth_norm_factor, hubs_norm_factor;

        for(int i = 0; i < this.size; i++)
        {
            nodes[i].computeAuth(this.nodesMap);
        }
        auth_norm_factor = 0.0f;
        for(int i = 0; i < this.size; i++)
        {
            auth_norm_factor += Math.pow(nodes[i].getAuth(), 2);
        }
        auth_norm_factor = Math.sqrt(auth_norm_factor);
        for(int i = 0; i < this.size; i++)
        {
            nodes[i].normalizeAuth(auth_norm_factor);
        }


        for(int i = 0; i < this.size; i++)
        {
            nodes[i].computeHubs(this.nodesMap);
        }
        hubs_norm_factor = 0.0f;
        for(int i = 0; i < this.size; i++)
        {
            hubs_norm_factor += Math.pow(nodes[i].getHubs(), 2);
        }
        hubs_norm_factor = Math.sqrt(hubs_norm_factor);

        for(int i = 0; i < this.size; i++)
        {
            nodes[i].normalizeHubs(hubs_norm_factor);
        }

        getAuths();
        getHubs();
    }

    public boolean hasConverged()
    {
        if((previous_auths == null) || (previous_hubs == null))
        {
            return false;
        }

        for(int i = 0; i < this.size; i++)
        {
            if(Math.abs(this.previous_auths[i] - this.auths[i]) >= this.epsilon)
            {
                return false;
            }

            if(Math.abs(this.previous_hubs[i] - this.hubs[i]) >= this.epsilon)
            {
                return false;
            }
        }
        return true;
    }

    private void getAuths()
    {
        this.previous_auths = this.auths;
        this.auths = new double[this.size];

        for(int i = 0; i < this.size; i++)
        {
            this.auths[i] = this.nodes[i].getAuth();
        }
    }

    private void getHubs()
    {
        this.previous_hubs = this.hubs;
        this.hubs = new double[this.size];

        for(int i = 0; i < this.size; i++)
        {
            this.hubs[i] = this.nodes[i].getHubs();
        }
    }

    public void printScores(int count, HashMap<Integer, String> idToLink) throws IOException
    {
        int limit = (count > this.size)? this.size : count;
        FileWriter writer = new FileWriter("auths.txt");
        String entry = null;

        Arrays.sort(this.nodes, new Comparator<Node>(){
            public int compare(Node a, Node b)
            {
                return (b.getAuth()).compareTo(a.getAuth());
            }
        });
        for(int i = 0; i < limit; i++)
        {
            entry = Double.toString(this.nodes[i].getAuth());
            entry += "\t" + idToLink.get(this.nodes[i].getId())  + "\n";
            writer.write(entry);
        }
        writer.close();

        writer = new FileWriter("hubs.txt");
        Arrays.sort(this.nodes, new Comparator<Node>(){
            public int compare(Node a, Node b)
            {
                return (b.getHubs()).compareTo(a.getHubs());
            }
        });
        for(int i = 0; i < limit; i++)
        {
            entry = Double.toString(this.nodes[i].getHubs());
            entry += "\t" + idToLink.get(this.nodes[i].getId())  + "\n";
            writer.write(entry);
        }
        writer.close();
    }
}

public class HITS
{
    private static int id;

    static HashMap<Integer, HashSet<Integer>> allInlinks;
    static HashMap<Integer, HashSet<Integer>> allOutlinks;
    static HashMap<String, Integer> linkToId;
    static HashMap<Integer, String> idToLink;
    static HashSet<String> crawled;
    static HashSet<Integer> root;
    static HashSet<Integer> augment;
    static HashSet<Integer> base;
    static HashMap<Integer, HashSet<Integer>> inlinks;
    static HashMap<Integer, HashSet<Integer>> outlinks;

    static
    {
        id = 1;
        allInlinks = null;
        allOutlinks = null;
        linkToId = new HashMap<String, Integer>();
        idToLink = new HashMap<Integer, String>();
        crawled = new HashSet<String>();
        root = new HashSet<Integer>();
        augment = new HashSet<Integer>();
        base = null;
        outlinks = new HashMap<Integer, HashSet<Integer>>();
        inlinks = new HashMap<Integer, HashSet<Integer>>();
    }

    public static void main(String[] args) throws IOException
    {
        readCrawled();
        System.out.println("Read crawled.");
        getAllOutlinks();
        System.out.println("Read all outlinks.");
        getAllInlinks();
        System.out.println("Read all inlinks.");
        readResults(50);
        System.out.println("Read resullts.");
    //    readInlinks();
    //    System.out.println("Read inlinks.");
        buildBase();
        System.out.println("Base built.");
        //System.out.println(test());
        computeHITS();
    }

    static Integer getId(String link)
    {
        if(linkToId.containsKey(link))
        {
            return linkToId.get(link);
        }
        linkToId.put(link, id);
        idToLink.put(id, link);
        return id++;
    }

    static void readCrawled() throws IOException
    {
        String crawled_file = "/home/kishore/Documents/IR/code/hw4/crawled_links.txt";
        BufferedReader cr = new BufferedReader(new FileReader(crawled_file));

        String line;

        while((line=cr.readLine()) != null)
        {
            crawled.add(line);
        }
        cr.close();
    }

    static void readResults(int max_inlinks) throws IOException
    {
        String result_file = "data.txt";
        BufferedReader results = new BufferedReader(new FileReader(result_file));

        String[] parts = null;
        String line = "";
        int link_id;
        int outlink_id, inlink_id;
        HashSet<Integer> outs;
        HashSet<Integer> ins;
        HashSet<Integer> ins_selected;
        int inlink_count;

        int less = 0, more = 0, broken = 0;
        boolean entered = false;

        while((line=results.readLine()) != null)
        {
            parts = line.split(" ");
            link_id = getId(parts[0]);
            root.add(link_id);
            outs = getOutlinksInCrawled(link_id);
            for(Iterator<Integer> i = outs.iterator(); i.hasNext();)
            {
                augment.add(i.next());
            }
            outlinks.put(link_id, outs);

            ins = getInlinksInCrawled(link_id);
            ins_selected = new HashSet<Integer>();
            inlink_count = 0;
            for(Iterator<Integer> j = ins.iterator(); j.hasNext();)
            {
                if(inlink_count >= max_inlinks)
                {
                    broken++;
                    break;
                }
                inlink_id = j.next();
                augment.add(inlink_id);
                ins_selected.add(inlink_id);
                inlink_count++;
            }
            inlinks.put(link_id, ins_selected);
            /*
            if(parts.length < 2)
            {
                continue;
            }

            if(entered)
            {
                more++;
            }
            entered = true;
            for(int i = 1; i < parts.length; i++)
            {
                if(! crawled.contains(parts[i]))
                {
                    continue;
                }
                if(entered)
                {
                    entered = false;
                }
                less++;
                outlink_id = getId(parts[i]);
                augment.add(outlink_id);
                outs.add(outlink_id);
            }
            outlinks.put(link_id, outs);
            */
        }
        System.out.println("broken: " + Integer.toString(broken));
        results.close();
        int oo = 0;
        for(Map.Entry<Integer, HashSet<Integer>> e : outlinks.entrySet())
        {
            if(e.getValue().size() == 0)
                oo++;
        }
        System.out.println("oo: " + Integer.toString(oo));
        System.out.println("Less: " + Integer.toString(less));
        System.out.println("More: " + Integer.toString(more));
    }

    private static void getAllInlinks() throws IOException
    {
        allInlinks = new HashMap<Integer, HashSet<Integer>>();
        String inlinks_file = "/home/kishore/Documents/IR/code/hw3/merged_inlinks_kish.txt";
        BufferedReader in = new BufferedReader(new FileReader(inlinks_file));

        String[] parts = null;
        String line = "";
        int link_id;
        HashSet<Integer> ins = null;

        while((line=in.readLine()) != null)
        {
            parts = line.split(" ");
            link_id = getId(parts[0]);
            ins = new HashSet<Integer>();
            for(int i = 1; i < parts.length; i++)
            {
                if(crawled.contains(parts[i]))
                {
                    ins.add(getId(parts[i]));
                }
            }
            allInlinks.put(link_id, ins);
        }
        in.close();
    }

    private static void getAllOutlinks() throws IOException
    {
        allOutlinks = new HashMap<Integer, HashSet<Integer>>();
        String outlinks_file = "/home/kishore/Documents/IR/code/hw3/outlinks_merged_kish.txt";
        BufferedReader out = new BufferedReader(new FileReader(outlinks_file));

        String[] parts = null;
        String line = "";
        int link_id;
        HashSet<Integer> outs = null;

        while((line=out.readLine()) != null)
        {
            parts = line.split(" ");
            link_id = getId(parts[0]);
            outs = new HashSet<Integer>();
            for(int i = 1; i < parts.length; i++)
            {
                if(crawled.contains(parts[i]))
                {
                    outs.add(getId(parts[i]));
                }
            }
            allOutlinks.put(link_id, outs);
        }
        out.close();
    }

    private static HashSet<Integer> getInlinksInCrawled(Integer id)
    {
        if(allInlinks.containsKey(id))
        {
            return allInlinks.get(id);
        }
        return new HashSet<Integer>();

    }

    private static HashSet<Integer> getOutlinksInCrawled(Integer id)
    {
        if(allOutlinks.containsKey(id))
        {
            return allOutlinks.get(id);
        }
        return new HashSet<Integer>();
    }
    /*
    static void readInlinks()
    {
        Integer link_id;
        HashSet<Integer> ins = null;
        for(Iterator<Integer> i = root.iterator(); i.hasNext();)
        {
            link_id = i.next();
            if(! inlinks.containsKey(link_id))
            {
                ins = getInlinksInCrawled(link_id);
                for(Iterator<Integer> j = ins.iterator(); j.hasNext();)
                {
                    augment.add(j.next());
                }
                inlinks.put(link_id, ins);
            }

        }
    }
    */
    static void buildBase()
    {
        Integer link_id, inlink_id, outlink_id;
        HashSet<Integer> allInlinks, allOutlinks, baseInlinks, baseOutlinks;

        base = new HashSet<Integer>(root);
        base.addAll(augment);
        System.out.println("Base set: " + Integer.toString(base.size()));

        for(Iterator<Integer> i = augment.iterator(); i.hasNext();)
        {
            link_id = i.next();
            allInlinks = getInlinksInCrawled(link_id);
            allOutlinks = getOutlinksInCrawled(link_id);

            baseInlinks = new HashSet<Integer>();
            for(Iterator<Integer> j = allInlinks.iterator(); j.hasNext();)
            {
                inlink_id = j.next();
                if(base.contains(inlink_id))
                {
                    baseInlinks.add(inlink_id);
                }
            }
            inlinks.put(link_id, baseInlinks);

            baseOutlinks = new HashSet<Integer>();
            for(Iterator<Integer> j = allOutlinks.iterator(); j.hasNext();)
            {
                outlink_id = j.next();
                if(base.contains(outlink_id))
                {
                    baseOutlinks.add(outlink_id);
                }
            }
            outlinks.put(link_id, baseOutlinks);
        }
    }

    static int test()
    {
        System.out.println("Inlinks: " + Integer.toString(inlinks.size()));
        System.out.println("Outlinks: " + Integer.toString(outlinks.size()));
        int in = 0, out = 0;
        for(Map.Entry<Integer, HashSet<Integer>> entry: inlinks.entrySet())
        {
            if(! base.contains(entry.getKey()))
            {
                return -1;
            }
            if(entry.getValue().size() == 0)
            {
                in++;
            }
            for(Iterator<Integer> i = entry.getValue().iterator(); i.hasNext();)
            {
                if(! base.contains(i.next()))
                {
                    return -1;
                }
            }
        }

        for(Map.Entry<Integer, HashSet<Integer>> entry: outlinks.entrySet())
        {
            if(! base.contains(entry.getKey()))
            {
                return -1;
            }
            if(entry.getValue().size() == 0)
            {
                out++;
            }
            for(Iterator<Integer> i = entry.getValue().iterator(); i.hasNext();)
            {
                if(! base.contains(i.next()))
                {
                    return -1;
                }
            }
        }
        System.out.println("In: " + Integer.toString(in));
        System.out.println("Out: " + Integer.toString(out));
        return 0;
    }

    static void computeHITS() throws IOException
    {
        int base_size = base.size();

        Graph graph = new Graph(base_size);
        graph.initialize(base, inlinks, outlinks);

        int count = 0;

        while(!graph.hasConverged())
        {
            graph.iterate();
            count++;
            System.out.println("Iteration: " + Integer.toString(count));
        }
        graph.printScores(500, idToLink);
    }
}
