package ordergroup.pkg;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Order {
    String Order_No;
    int Order_Width;
    int Set_Width;
    String Grade;
    Date Delivery_Date;
    int BTR_Qty;
    String product;
    String L1_Group;
    String L2_Group;
    String L3_Group;
    String L4_Group;
    double BTC_Qty;
    int Bucket;
    String Grade_Group;
    String VD_TYPE;
    String GRADE_TYPE;
    String Rolling_MILL;
    String Scrafing_Group;

    Order(String Order_No, int Order_Width, String Grade, Date Delivery_Date, int BTR_Qty, String product) {
        this.Order_No = Order_No;
        this.Order_Width = Order_Width;
        this.Grade = Grade;
        this.Delivery_Date = Delivery_Date;
        this.BTR_Qty = BTR_Qty;
        this.product = product;
        this.BTC_Qty = BTR_Qty * 1.1;
    }
}

public class OrderGroup {
	 public static int count=1;
	public static void main(String[] args) {
		 try {
			 Map<String, String> GradeMix = readGradeMix();
			 List<Order> order_details = readOrderdetailsfromfile();
	         Map<String, String[]> Grade_Details = readGradeDetails("D:\\input\\Grade_Details.txt");
	         calculateBuckets(order_details);
	         L1_Groups(order_details, GradeMix);
	         L2_Groups(order_details);
	         L3_Groups(order_details);
	         L4_Groups(order_details, Grade_Details);
	         OutputCSV(order_details,"D:\\input\\Order_Details_Output.csv");
			 System.out.println("Done");
		 } catch (Exception e) {
	            e.printStackTrace();
	        }
		
		}
	
	 public static List<Order> readOrderdetailsfromfile() throws Exception
	 {
        List<Order> order_details = new ArrayList<>();
        SimpleDateFormat[] dateFormats ={new SimpleDateFormat("dd-MM-yyyy"),new SimpleDateFormat("dd-MMM-yyyy")};
        try {
        	FileReader fr=new FileReader("D:\\input\\Order_details.txt");
        	BufferedReader br = new BufferedReader(fr);
            String line;
            br.readLine();
            System.out.println("Reading orders from file...");
            
            while ((line = br.readLine()) != null) {
                String[] value = line.split("\t");
                Date deliveryDate = null;
//                
                for (int i = 0; i < dateFormats.length; i++) {
                    try {
                        deliveryDate = dateFormats[i].parse(value[3]);
                        break;
                    } catch (Exception e) {
                    }
                }
                order_details.add(new Order(value[0],Integer.parseInt(value[1]),value[2],deliveryDate,Integer.parseInt(value[4]),value[5]));
            }
        }
        catch(Exception e)
        {
        	System.out.println("Error is "+e);
        }
        return order_details;
    }
	public static Map<String, String> readGradeMix() throws Exception 
	{
	        Map<String, String> GradeMix = new HashMap<>();
	        try {
	        	FileReader fr=new FileReader("D:\\input\\Grade_Mix.txt");
	        	BufferedReader br = new BufferedReader(fr);
	            String line;
	            br.readLine(); 
	            while ((line = br.readLine()) != null) {
	                String[] values = line.split("\t");
	                GradeMix.put(values[0], values[1]);
	            }
	        } 
	        catch(Exception e)
	        {
	        	System.out.println("Error is "+e);
	        }
	        return GradeMix;
	 }
	public static Map<String,String[]> readGradeDetails(String fileName) throws Exception {
        Map<String, String[]> Grade_Details = new HashMap<>();
        try {
        	FileReader fr=new FileReader("D:\\input\\Grade_Details.txt");
        	BufferedReader br = new BufferedReader(fr);
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] value = line.split("\t");
                Grade_Details.put(value[0], new String[]{value[1], value[2], value[3], value[4], value[5], value[6]});
            }
        } 
        catch(Exception e)
        {
        	System.out.println("Error is 1 "+e);
        }
        return Grade_Details;
    }
	public static void calculateBuckets(List<Order> order_details) {
		 int Bucket_Days=3;
        Calendar Ref_date = Calendar.getInstance();
        Ref_date.set(2024,7,15);

        for (Order order : order_details) {
            Calendar Order_Date = Calendar.getInstance();
            Order_Date.setTime(order.Delivery_Date);
            int Day_Diff  = (int)((Order_Date.getTimeInMillis() - Ref_date.getTimeInMillis())/ (1000 * 60 * 60 * 24));
            int Bucket = (Day_Diff / Bucket_Days) + 1;
            	order.Bucket = Bucket;
        }
    }
	public static void L1_Groups(List<Order> order_details, Map<String, String> Grade_Mix) {
		 Map<String, List<Order>> L1_Groups = new HashMap<>();
	        Map<String, String> Group_Mapping = new HashMap<>();

	        int Counter = 1;

	        for (Order om : order_details) {
	            String Comp_Grades = Grade_Mix.getOrDefault(om.Grade, om.Grade);

	            if (!Group_Mapping.containsKey(Comp_Grades)) {
	                String groupName = "L1G" + Counter++;
	                Group_Mapping.put(Comp_Grades, groupName);
	            }
	            String GroupName = Group_Mapping.get(Comp_Grades);

	            om.L1_Group = GroupName;
	            if (!L1_Groups.containsKey(GroupName)) {
	            	L1_Groups.put(GroupName, new ArrayList<>());
	            }
	            L1_Groups.get(GroupName).add(om);
	        }
	    }
	 public static void L2_Groups(List<Order> order_details) {
		 Map<String, List<Order>> L1_Groups = new HashMap<>();
	        for (Order om : order_details) {
	            if (!L1_Groups.containsKey(om.L1_Group)) {
	            	L1_Groups.put(om.L1_Group, new ArrayList<>());
	            }
	            L1_Groups.get(om.L1_Group).add(om);
	        }

	        for (Map.Entry<String, List<Order>> entry : L1_Groups.entrySet()) {
	            List<Order> Group = entry.getValue();
	            Collections.sort(Group, new Comparator<Order>() {
	                @Override
	                public int compare(Order o1, Order o2) {
	                    return Integer.compare(o2.Order_Width, o1.Order_Width);
	                }
	            });

	            List<Order> L2_Group = new ArrayList<>();
	            int currentWidth = -1;
	            String l2GroupName = "L2G1";
	            int groupCounter = 1;

	            for (Order tm :Group) {
	                int ceilingWidth = ((tm.Order_Width + 24) / 25) * 25;
	                int newWidth = ceilingWidth + 15;
	                int diff = newWidth - tm.Order_Width;
	                
	                if (currentWidth == -1 || Math.abs(newWidth - currentWidth) > 50) {
	                    l2GroupName = "L2G" + groupCounter++;
	                    currentWidth = ceilingWidth;
	                } else {
	                    currentWidth = newWidth;
	                }
	                
	                tm.Set_Width = currentWidth;
	                tm.L2_Group = l2GroupName;
	                L2_Group.add(tm);
	            }
	        }
	 }
	 public static void L3_Groups(List<Order> order_details) {
	        Map<String, List<Order>> L2_Groups = new HashMap<>();
	        for (Order om : order_details) {
	            if (!L2_Groups.containsKey(om.L2_Group)) {
	            	L2_Groups.put(om.L2_Group, new ArrayList<>());
	            }
	            L2_Groups.get(om.L2_Group).add(om);
	        }

	        for (Map.Entry<String, List<Order>> entry : L2_Groups.entrySet()) {
	            List<Order> Group = entry.getValue();
	            Collections.sort(Group, new Comparator<Order>() {
	                @Override
	                public int compare(Order o1, Order o2) {
	                    return Integer.compare(o2.Set_Width, o1.Set_Width);
	                }
	            });

	            List<Order> L3_Group = new ArrayList<>();
	            int Current_Width = -1;
	            String L3_GroupName = "L3G1";
	            int groupCounter = 1;

	            for (Order om : Group) {
	                if (Current_Width == -1 || Math.abs(om.Set_Width - Current_Width) > 75) {
	                	L3_GroupName = "L3G" + groupCounter++;
	                	Current_Width = om.Set_Width;
	                }
	                om.L3_Group = L3_GroupName;
	                L3_Group.add(om);
	            }
	        }
	    }
	 public static void L4_Groups(List<Order> order_details, Map<String, String[]> Grade_Details) {
	        Map<String, List<Order>> Grade_Groups = new HashMap<>();
	        for (Order om : order_details) {
	            String[] details = Grade_Details.get(om.Grade);
	            if (details != null) {
	                om.Grade_Group = details[0];
	                om.VD_TYPE = details[1];
	                om.GRADE_TYPE = details[2];
	                om.Rolling_MILL = details[3];
	                om.Scrafing_Group = details[4];
	            }
	            if (!Grade_Groups.containsKey(om.Grade_Group)) {
	            	Grade_Groups.put(om.Grade_Group, new ArrayList<>());
	            }
	            Grade_Groups.get(om.Grade_Group).add(om);
	        }

	        for (Map.Entry<String, List<Order>> entry : Grade_Groups.entrySet()) {
	            String gradeGroup = entry.getKey();
	            List<Order> group = entry.getValue();
	            Collections.sort(group, new Comparator<Order>() {
	                @Override
	                public int compare(Order o1, Order o2) {
	                    return Integer.compare(o2.Set_Width, o1.Set_Width);
	                }
	            });

	            List<Order> L4_Group = new ArrayList<>();
	            int currentWidth = -1;
	            String l4GroupName = "L4G1";
	            int groupCounter = 1;

	            for (Order om : group) {
	                if (currentWidth == -1 || Math.abs(om.Set_Width - currentWidth) > 25) {
	                    l4GroupName = "L4G" + groupCounter++;
	                    currentWidth = om.Set_Width;
	                }
	                om.L4_Group = l4GroupName;
	                L4_Group.add(om);
	            }
	        }
	    }
	 public static void OutputCSV(List<Order> order_details, String outputFile) throws IOException {
	        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	        BufferedWriter writer = null;
	        try {
	            writer = new BufferedWriter(new FileWriter(outputFile));
	            writer.write("Order_No,Order_Width,Set_Width,Grade,Delivery_Date,BTR_Qty,Product,L1_Group,L2_Group,L3_Group,L4_Group,BTC_Qty,Bucket,Grade_Group,VD_TYPE,GRADE_TYPE,Rolling_MILL,Scrafing_Group");
	            writer.newLine();

	            for (Order om : order_details) {
	                String formattedDate = (om.Delivery_Date != null) ? sdf.format(om.Delivery_Date) : "Invalid Date";
	                writer.write(String.format("%s,%d,%d,%s,%s,%d,%s,%s,%s,%s,%s,%.2f,%d,%s,%s,%s,%s,%s%n",
	                    om.Order_No,om.Order_Width,om.Set_Width,om.Grade,formattedDate,om.BTR_Qty,om.product,
	                    om.L1_Group,om.L2_Group,om.L3_Group,om.L4_Group,om.BTC_Qty,om.Bucket,
	                    om.Grade_Group,om.VD_TYPE,om.GRADE_TYPE,om.Rolling_MILL,om.Scrafing_Group));
	            }
	        } catch (Exception e) {
	            System.out.println("Error in the printOutput method: " + e);
	        } 
	    }
}
