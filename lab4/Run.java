import java.util.*;
import java.io.*;
public class Run {
    //Final numbers to be printed out global bc several methods use it
    public static int totalAverage = 0;
    public static int totalEvicted = 0;
    public static int totalResFinalNum = 0;
    public static int faultsFinalNum = 0;
    //List of process and done ones
    public static List<P> done = new ArrayList<P>();
    public static List<P> allp = new ArrayList<P>();
    //Random list
    public static ArrayList<Integer> randomList = new ArrayList<Integer>();
    //User input

    public static String algo_name = "noalgo"; //Algo name
    public static int machine_size = 0; //Machine Size
    public static int page_size = 0; //Page Size
    public static int proc_size = 0; //Process Size
    public static int job_mix = 0; //Job Mix
    public static int num_ref = 0; //Refernce number
    public static int debug_level = 0; //debug level
    public static int num = 0; //num we are at

    //Begin
    public static void main (String args[]) throws IOException {
        //Read random file Scanner
        try{
            Scanner ran = new Scanner(new FileInputStream("random-numbers.txt"));
            while(ran.hasNextInt()){
              randomList.add(ran.nextInt());
            }
          }catch(Error E){
            System.out.println("Error with Random files txt ʕ　·ᴥ·ʔ ");
          }
        //read user input

        try{
          //User gave wrong args
          if(args.length < 6){
            System.out.println("Too little arguments ʕ　·ᴥ·ʔ ");
            System.exit(1);
      }else if(args.length > 7){
        System.out.println("Too MANY arguments ʕﾉಠᴥಠʔﾉ ");
        System.exit(1);

      }
      else{
        //Pass given in args to vars to print
          machine_size = Integer.parseInt(args[0]);
          page_size =Integer.parseInt(args[1]) ;
          proc_size = Integer.parseInt(args[2]) ;
          job_mix = Integer.parseInt(args[3]) ;
          num_ref =Integer.parseInt(args[4]) ;
          algo_name = args[5] ;
          debug_level = Integer.parseInt(args[6]) ;
          num = machine_size / page_size;
      }

    }
    //If there is an error display it
    catch(Error e){
      System.out.println(e);
      }
        //Print what was given by input
        printGiven(machine_size,page_size,proc_size,job_mix, num_ref, algo_name,debug_level);
        //Determing job mix level
        jobMixlevel(job_mix, num_ref, proc_size, page_size);
        //Begin iterating through table
        start();
        //Print sections meaning each p
        sections();
        //Print overall stats
        overall();
        //   //Print end of program
        // printdone();

    }
    public static void start(){
      //--Start iterations of adding processes to a frame table---/
      FrameT[] frametable = new FrameT[num]; //create a frame table to store proc [Proc][Proc][Proc]
      List<FrameT> FIFO = new ArrayList<FrameT>(); //First in first out
      List<FrameT> LRU = new ArrayList<FrameT>(); //Last Recently used
      ListIterator<P> it = allp.listIterator();      //Links and iterates
      int cycle = 1; //Time
      int hit = -2; //Check to see if it is a page hit
      int q = 0; //Round Robin Quanta

      //While terminated is not done
      while (allp.size()!=done.size()) {
        //Define curr proc

          P process;
          //Check if it has a next element
          if (it.hasNext()) {
              process = it.next();
          }
          else {
            //else
            it = allp.listIterator();
            process = it.next();
          }

          while (q != 3) {
            //Given in notes how to calculate the current address
            int currentAdress = (111 * process.pnum) % process.psize;
            //Quanta is not 3 then we still can run
            int pnum; //define pnum
            int tablePnum; //define table num
              if (!process.firstpass) {
                //First time we have seen it
                  process.firstpass = true; //Set it to true
                  process.currAdd = currentAdress; //Calculate pg size and how big the iterations will be
                  process.actualpnum = process.currAdd / process.pgsize; //Iterations

              }
              else {
                  //We have seen it we can iterate normally
                  process.currAdd = process.next;
                  //Current p num
                  process.actualpnum = process.currAdd / process.pgsize;
              }

              pnum = process.pnum; //Set it to current P number
              tablePnum = process.actualpnum; //Update Table
              hit = 0; //Say it was a hit

              for (int i = 0; i < frametable.length; i++) {
                //Push away element and add the new one
                  if (frametable[i] != null ) {
                      if(pnum ==  frametable[i].pnum){
                          if(frametable[i].tablePnum == tablePnum){
                             //Last recently used
                              LRU.remove(frametable[i]);
                              //Add to the table
                              LRU.add(frametable[i]);
                              //hit
                              hit = 1;
                  }
                }
              }
            }

              if (hit == 0) {
                //Add counter for p x-1 remaning if q >3
                process.pcounter++;
                boolean processed = false; //Check to see if we have procesed next
                for (int i = frametable.length-1; i >= 0; i--) {
                    if (frametable[i] == null) { // If frames are free do whole dance of adding
                        FrameT temp = new FrameT(process.pgsize, i);
                        temp.free = true;
                        temp.tablePnum = process.actualpnum;
                        temp.pnum =  process.pnum;
                        temp.nextpass = cycle;
                        temp.tablecurrp = process;
                        frametable[i] = temp;
                        LRU.add(temp);
                        FIFO.add(temp);
                        processed = true;
                        break;
                    }
                }
                //If we have not processed the p then read what kind of algo is it
                if (!processed) {
                    if (algo_name.equals("lru")) {
                        FrameT temp = LRU.get(0);
                        LRU.remove(0);
                        int residency = cycle - temp.nextpass;
                        tableEnter( temp,  process,  cycle);
                        LRU.add(temp);
                    }
                    else if (algo_name.equals("fifo")) {
                      FrameT temp = FIFO.get(0);
                      FIFO.remove(0);
                      int residency = cycle - temp.nextpass;
                      tableEnter( temp,  process,  cycle);
                      FIFO.add(temp);
                    }
                    else if (algo_name.equals("random")) {
                        int r = randomList.get(0);
                        randomList.remove(0);
                        int i = r % frametable.length;
                        FrameT temp = frametable[i];
                        tableEnter( temp,  process,  cycle);
                    }
                    else{
                      System.out.println("That algo does not exist ʕ　·ᴥ·ʔ");
                      System.exit(1);
                    }

                }
              }
              victimEvict( process); //Choose a victim to evict based on specs
              q++; //Increase quanta
              cycle++; //Time also increases
              process.refcurr++; //Current ALSo increases

              if (process.refcurr == process.refnum) {
                //If the ref number is the same as the process we are done set q = 3
                  done.add(process);
                  q = 3;
              }
          }
          q = 0;
      }
    }
    //Method to evict a victim
    public static void victimEvict(P process){
      int r = randomList.get(0);
      randomList.remove(0);
      double y = r / (Integer.MAX_VALUE + 1d);
      if (y < process.a) {
          process.next = (process.currAdd + 1) % process.psize;
      }
      else if (y < process.a + process.b) {
          process.next = (process.currAdd - 5 + process.psize) % process.psize;
      }
      else if (y < process.a + process.b + process.c) {
          process.next = (process.currAdd + 4) % process.psize;
      }
      else {
          r = randomList.get(0);
          randomList.remove(0);
          process.next = r % process.psize;
      }
    }
    //Enter P into the table
    public static void tableEnter(FrameT temp, P process, int cycle){
      int residency = cycle - temp.nextpass;
      temp.tablecurrp.victims ++;
      temp.tablecurrp.restime += residency;
      temp.free = true;
      temp.pnum =  process.pnum;
      temp.tablePnum = process.actualpnum;
      temp.tablecurrp = process;
      temp.nextpass = cycle;
    }
    //Print overall stats
    public static void overall(){
      if (totalEvicted == 0) {
          System.out.println("Total number of faults is " + faultsFinalNum + ".");
          System.out.println("No evcitions: the overall average residency is undefined.");

      }
      else {
          totalAverage = totalResFinalNum / (int)totalEvicted;
          System.out.println("Total number of faults is " + faultsFinalNum + " and the overall average residency is " + totalAverage );
      }
    }
    //Print sections
    public static void sections(){
      for (P process : done) {
          if (process.victims == 0) {
              System.out.println("Process " + process.pnum + " had " + process.pcounter + " faults.");
              System.out.println("No evictions: the average residency is none.");
              totalResFinalNum += process.restime;
          }
            else {
                int averageResidency = process.restime / (int)process.victims;
                totalEvicted += process.victims;
                totalResFinalNum += process.restime;
                System.out.println("Faults for process " + process.pnum + " is " + process.pcounter);
                System.out.println("Average Residency " + " is " + averageResidency);
                totalAverage += averageResidency;
            }
          faultsFinalNum += process.pcounter;
      }
    }
    //Print given
    public static void printGiven(  int machine_sizex, int page_sizex, int proc_sizex,int job_mixx,int  num_refx, String algo_namex, int debug_levelx){
      System.out.println("The Machine Size is " + machine_sizex);
      System.out.println("The page size is " + page_sizex);
      System.out.println("The process size is " + proc_sizex);
      System.out.println("The job mix number is " + job_mixx);
      System.out.println("The number of references per process is " + num_refx);
      System.out.println("The replacement algorithm is " + algo_namex );
      System.out.println("The level of debugging output is "+ debug_levelx + "\n");

    }
    public static void printdone(){
      System.out.println("> THE END :)jA <");
    }
    //Determine job mix Specs given in lab
    public static void jobMixlevel(int job_mix, int num_ref, int proc_size, int page_size){
      if(job_mix == 1){
        //double a,double b,double c,int num_ref,int page_size,int firstnumber ,int proc_size
              allp.add(new P(1, 0, 0, num_ref, page_size, 1, proc_size));

          }
      else if(job_mix == 2){
              allp.add(new P(1, 0, 0, num_ref, page_size, 1, proc_size));
              allp.add(new P(1, 0, 0, num_ref, page_size, 2, proc_size));
              allp.add(new P(1, 0, 0, num_ref, page_size, 3, proc_size));
              allp.add(new P(1, 0, 0, num_ref, page_size, 4, proc_size));

          }
      else if(job_mix == 3){

        allp.add(new P(0, 0, 0, num_ref, page_size, 1, proc_size));
        allp.add(new P(1, 0, 0, num_ref, page_size, 2, proc_size));
        allp.add(new P(1, 0, 0, num_ref, page_size, 3, proc_size));
        allp.add(new P(1, 0, 0, num_ref, page_size, 4, proc_size));


          }
          else if(job_mix == 4){
            allp.add(new P(0.75, 0.25, 0, num_ref, page_size, 1, proc_size));
            allp.add(new P(0.75, 0, 0.25, num_ref, page_size, 2, proc_size));
            allp.add(new P(0.75, 0.125, 0.125, num_ref, page_size, 3, proc_size));
            allp.add(new P(0.5, 0.125, 0.125, num_ref, page_size, 4, proc_size));
          }
        else{
          //Error catch
          System.out.println("Wrong Job Mix ʕﾉಠᴥಠʔﾉ ");
          System.exit(1);

        }

    }


}
