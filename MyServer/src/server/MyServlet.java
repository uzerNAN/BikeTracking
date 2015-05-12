package server;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.sql.DataSource;



//import org.json.simple.JSONObject;
//import com.fasterxml.jackson.core.*;
import org.json.*;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Context;
 
@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    public MyServlet() {
        super();
 
    }
    
    
 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getOutputStream().println("Hello World.");  
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
        try {
            int length = request.getContentLength();
            byte[] input = new byte[length];
            ServletInputStream in = request.getInputStream();
            int i, count=0;
            while ((i = in.read(input, count, input.length-count)) != -1) {
                count +=i;
            }
            in.close();
 
            String receivedString = new String(input);
            System.out.println("val = " + receivedString);
            
            Connection connection = getConnection();
            JSONArray jsonData;
            
    		try {
    			jsonData = new JSONArray(receivedString);
    			JSONObject Data;
    			PreparedStatement preparedStatement = connection.prepareStatement("insert into Node values (?, ?, ?, ?, ?, ?)");
    			
    			Data = (JSONObject) jsonData.get(0);
    			
    			
    			int SessionId, SessionIdSQL;
    			
    			int sid = Integer.parseInt((String) Data.get("SID"));
    			Double lat = Double.parseDouble((String) Data.get("LATITUDE"));
    			Double lon = Double.parseDouble((String) Data.get("LONGITUDE"));
    			long time = Long.parseLong((String) Data.get("TIME"));
    			Double speed = Double.parseDouble((String) Data.get("SPEED"));
    			Double acc = Double.parseDouble((String) Data.get("ACCURACY"));
    			
    			
    			SessionId = sid;
    			SessionIdSQL = generateID(connection);
    			
    		    preparedStatement.setInt(1, SessionIdSQL);
    		    preparedStatement.setLong(2, time);
    		    preparedStatement.setDouble(3, lon);
    		    preparedStatement.setDouble(4, lat);
    		    preparedStatement.setDouble(5, speed);
    		    preparedStatement.setDouble(6, acc);
    		    preparedStatement.executeUpdate();
    		    
    			for(int n = 1; n < jsonData.length(); n++){
    				
    				Data = (JSONObject) jsonData.get(n);
    				sid = Integer.parseInt((String) Data.get("SID"));
    				if (SessionId != sid){
    					SessionIdSQL = generateID(connection);
    					SessionId = sid;
    					
    				}

    				lat = Double.parseDouble((String) Data.get("LATITUDE"));
    				lon = Double.parseDouble((String) Data.get("LONGITUDE"));
    				time = Long.parseLong((String) Data.get("TIME"));
    			    
    			    preparedStatement.setInt(1, SessionIdSQL);
    			    preparedStatement.setLong(2, time);
    			    preparedStatement.setDouble(3, lon);
    			    preparedStatement.setDouble(4, lat);
    			    preparedStatement.executeUpdate();
    				
    				
    			}
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            
    		
            response.setStatus(HttpServletResponse.SC_OK);
            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
            writer.write("received");
            writer.flush();
            writer.close();
 
 
        } catch (IOException e) {
            try{
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(e.getMessage());
                response.getWriter().close();
            } catch (IOException ioe) {
            }
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
        }
    
    private Connection getConnection() {
        Connection connection = null;
        try {
          InitialContext context = new InitialContext();
          Context envCtx = (Context) context.lookup("java:comp/env");
          DataSource dataSource = (DataSource) envCtx.lookup("jdbc/cykeldata");
          connection = dataSource.getConnection();
        } catch (NamingException e) {
          e.printStackTrace();
        } catch (SQLException e) {
          e.printStackTrace();
        }
        return connection;
      }
    
    private int generateID(Connection conn){
    	try {
			Statement stmt = conn.createStatement(); 
			Statement stmt2 = conn.createStatement();
			stmt.executeUpdate("insert into Sessions values ()"); // Generate unique, autoincrement id
			ResultSet rs = stmt2.executeQuery("SELECT LAST_INSERT_ID()"); // Get the last auto-incremented id (specific for this scope)
			rs.next();
			int id = Integer.parseInt((String) rs.getString("LAST_INSERT_ID()")); 
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return 0;
    	
    }
    
    
}