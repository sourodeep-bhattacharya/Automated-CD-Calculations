import java.io.*;
import java.util.*;

public class Reader {

/*
    Atom Class:  
        - stores atomic number, abbreviation, and coordinate data of an individual atom in the molecule
        - Prints this information with the ret() method
*/
    class Atom {
        double number; 
        ArrayList<Double> coords; 

        public Atom(double number, ArrayList<Double> coords) {
            this.number = number;
            this.coords = coords; 
        }

        
        public void ret() {
            System.out.println(number+": ");
            for(int i = 0; i < coords.size(); i++) {
                System.out.println(coords.get(i));
            }
        }
    }

/*
    findAtoms() Method: 
        - finds the total number of atoms in the molecule
        - Inputs: name of Gaussian relaxed scan output file
        - Outputs: number of atoms 
*/

    public int findAtoms(String filename) {
        int n = 0; 
        String dat = "";
        try {
            File obj = new File(filename);
            Scanner reader = new Scanner(obj);
            while(reader.hasNext()) {
                if(reader.next().contains("NAtoms")){
                    dat = (reader.next());
                    break;
                }

            }
            
            n = Integer.parseInt(dat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return n;
    }

/*
    findReps() Method: 
        - finds the number of unique structures formed from adjusting a parameter (e.g. dihedral angle, bond length, etc.) during scan
        - Inputs: name of Gaussian relaxed scan output file
        - Outputs: number of structures (steps in scan)
*/

    
    public int findReps(String filename) {
        String line = "";
        try{
            File myObj = new File(filename);
            Scanner s = new Scanner(myObj);
            
            while (s.hasNext()) {
                if(s.nextLine().contains("Search for a local minimum"))
                {
                    line = s.nextLine();
                    break;
                }
                s.nextLine();
            }
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
        char[] array = {line.charAt(line.length() - 3), line.charAt(line.length() - 2), line.charAt(line.length() - 1)};
        String s = new String(array);
        int a = Integer.parseInt(s.trim());

        return a;
    }

/*
    scanFile() Method: 
        - searches relaxed scan output for markers that indicate where geometries are found
        - Inputs: name of Gaussian relaxed scan output file, number of structures
        - Outputs: HashMap containing a list of line numbers containing instances of "n out of   N"
            - n = current structures
            - N = total number of structures
            - Instances of this search string are followed by a geometry optimization step
            - Thus, after the last instance of "2 out of   6", the fully optimized geometry of the 2nd structure can be found
*/
    public HashMap<Integer, ArrayList<Integer>> scanFile(String fileName, int N) {
        HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
        int num = 6 - (N+"").length();
        String look = " out of";
        for(int i = 0; i < num; i++) {
            look += " ";
        }
        look += N;

        
        
        try {
            for (int i = 1; i <= N;i++) {
                ArrayList<Integer> list = new ArrayList<>();
                Scanner s = new Scanner(new File(fileName));
                int counter = 0;
                while (s.hasNext()) {
                    counter++;
                    String line = s.nextLine();

                    if(line.contains(" "+i+look)){
                        list.add(counter);
                    }
                    
                }
                map.put(i, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map; 
    }

/*
    processLines() Method: 
        - stores coordinate data from a cartesian geometry matrix into Atom objects
        - Inputs: list of lines from output file that contain a cartesian matrix
        - Outputs: list of Atom objects (each containing coordinate data)
            
*/
    public ArrayList<Atom> processLines(ArrayList<String> list) {
        ArrayList<Atom> atoms = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            Scanner finder = new Scanner(list.get(i));
            finder.nextDouble();
            double atom = finder.nextDouble();
            finder.nextDouble();
            ArrayList<Double> carts = new ArrayList<>();
            while(finder.hasNext()){
                carts.add(finder.nextDouble());
            }
            atoms.add(new Atom(atom, carts));
        }
        return atoms;
    }

/*
    saveGeoms() Method: 
        - navigates to fully optimized geometry for each structure and uses processLines() method to store data
        - Inputs: HashMap containing line numbers associated to geometries, output file name, number of atoms
        - Outputs: List containing lists of Atom objects
            - Each list of Atom objects represents a unique structure
            - Thus, a list of Atom lists is a list of unique structures
            
*/

    public ArrayList<ArrayList<Atom>> saveGeoms(HashMap<Integer, ArrayList<Integer>> map, String filename, int M) {
        ArrayList<ArrayList<Atom>> geoms = new ArrayList<>();

            try{
                for(int key : map.keySet()) {
                    ArrayList<Integer> list = map.get(key);
                    int line = list.get(list.size() - 1);
                    Scanner s = new Scanner(new File(filename));
                    for(int i = 0; i < line; i++) {
                        s.nextLine();
                    }
                    ArrayList<Atom> atoms = new ArrayList<>();

                    while(s.hasNext()) {
                        if (s.nextLine().contains("Standard orientation")){
                            s.nextLine(); s.nextLine(); s.nextLine(); s.nextLine();
                            ArrayList<String> data = new ArrayList<>();
                            for(int i = 0; i < M; i++) {
                                data.add(s.nextLine()+"\n");

                            } 
                            //add a feature for n-atoms
                            atoms = processLines(data);
                            break;
                        }
                        s.nextLine();
                    }
                    geoms.add(atoms);

                }
            }
            catch(Exception e){
                e.printStackTrace();
                
            }
        return geoms;
        
    }

/*
    readTemplate() Method: 
        - scans Gaussian input file template that a user can create 
            - user specifies checkpoint file name, number of processors, theory, basis set, etc. 
        - Inputs: name of template file
        - Outputs: List containing lines in template file

*/

    public ArrayList<String> readTemplate(String template) {
        ArrayList<String> lines = null;
        try {
            File f = new File(template);
            Scanner reader = new Scanner(f);

            lines = new ArrayList<String>();
            while(reader.hasNext()) {
                lines.add(reader.nextLine());
            }

            reader.close();
        } catch(Exception e) {
            e.getStackTrace();
        }
        return lines;
    }

/*
    makeInput() Method: 
        - writes user input from template and cartesian coordinate matrix into new Gaussian input file (.gjf)
        - Inputs: List of Atoms, list of lines from template file, name of template file
        - Outputs: N/A

*/
    public void makeInput(ArrayList<Atom> atoms, ArrayList<String> lines, String filename){
        try{
            
            HashMap<Integer, String> pt = new HashMap<>();
            pt.put(1, "H");
            pt.put(2, "He");
            pt.put(3, "Li");
            pt.put(4, "Be");
            pt.put(5, "B");
            pt.put(6, "C");
            pt.put(7, "N");
            pt.put(8, "O");
            pt.put(9, "F");
            pt.put(10, "Ne");
            pt.put(11, "Na");
            pt.put(12, "Mg");
            pt.put(13, "Al");
            pt.put(14, "Si");
            pt.put(15, "P");
            pt.put(16, "S");
            pt.put(17, "Cl");
            pt.put(18, "Ar");

            File input = new File(filename);
            FileWriter writer = new FileWriter(input);


            for(String line:lines) {
                writer.write(line+"\n");
            }

            for(Atom a:atoms) {
                writer.write("\s"+pt.get((int)a.number)+"\t");
                
                for(double coord:a.coords){
                    writer.write(coord+"\t");
                }
                writer.write("\n");
                
            }
            writer.close();
        }

        catch(Exception e) {
            e.getStackTrace();
        }
        
    }


    public static void main(String[] args) {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter output file name: ");
            String output = reader.readLine();
            System.out.println("Enter input file template name: ");
            String template = reader.readLine();
    
            Reader r = new Reader();
            int N =  r.findReps(output);
            int M = r.findAtoms(output);
    
             
            HashMap<Integer, ArrayList<Integer>> map = r.scanFile(output, N);
            System.out.println("Relevant Line Numbers: \n");
            for(int i = 1; i <= 19; i++) {
                System.out.println("Instances of "+i+" out of "+N+":");
                System.out.println(map.get(i));
                System.out.println("\n");
            }
    
            ArrayList<ArrayList<Atom>> ret = r.saveGeoms(map, output, M);
            
            ArrayList<String> lines = r.readTemplate(template);
            
            for(int i = 0; i < N; i++){
                String name = "in"+(i+1)+".gjf";
                r.makeInput(ret.get(i),lines,name);
            }
            
        }catch(Exception e) {
            e.getStackTrace();
        }
            
    }
    
}


//add some documentation

//generalize for all molecules 