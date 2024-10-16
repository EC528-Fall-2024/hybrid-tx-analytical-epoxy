package org.dbos.apiary.postgresdemo;

import com.google.protobuf.InvalidProtocolBufferException;
import org.dbos.apiary.postgres.PostgresConnection;
import org.dbos.apiary.postgresdemo.functions.NectarAddPost;
import org.dbos.apiary.postgresdemo.functions.NectarGetPosts;
import org.dbos.apiary.postgresdemo.functions.NectarLogin;
import org.dbos.apiary.postgresdemo.functions.NectarRegister;
import org.dbos.apiary.utilities.ApiaryConfig;
import org.dbos.apiary.worker.ApiaryNaiveScheduler;
import org.dbos.apiary.worker.ApiaryWorker;
import org.dbos.apiary.client.ApiaryWorkerClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import org.dbos.apiary.postgresdemo.etl.ETLService; // ETL
import org.dbos.apiary.postgresdemo.clickhouse.ClickHouseService; // Clickhouse demo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes("logincredentials")
public class NectarController {
    ApiaryWorkerClient client;

    public NectarController() throws SQLException {
        ApiaryConfig.captureUpdates = true;
        ApiaryConfig.captureReads = true;
        ApiaryConfig.provenancePort = 5432;  // Store provenance data in the same database.

        PostgresConnection conn = new PostgresConnection("localhost", ApiaryConfig.postgresPort, "postgres", "dbos");
        conn.dropTable("WebsiteLogins"); // For testing.
        conn.dropTable("WebsitePosts"); // For testing.
        conn.createTable("WebsiteLogins", "Username VARCHAR(1000) PRIMARY KEY NOT NULL, Password VARCHAR(1000) NOT NULL");
        conn.createTable("WebsitePosts", "Sender VARCHAR(1000) NOT NULL, Receiver VARCHAR(1000) NOT NULL, PostText VARCHAR(10000) NOT NULL");

        ApiaryWorker apiaryWorker = new ApiaryWorker(new ApiaryNaiveScheduler(), 4, ApiaryConfig.postgres, ApiaryConfig.provenanceDefaultAddress);
        apiaryWorker.registerConnection(ApiaryConfig.postgres, conn);
        apiaryWorker.registerFunction("NectarRegister", ApiaryConfig.postgres, NectarRegister::new);
        apiaryWorker.registerFunction("NectarLogin", ApiaryConfig.postgres, NectarLogin::new);
        apiaryWorker.registerFunction("NectarAddPost", ApiaryConfig.postgres, NectarAddPost::new);
        apiaryWorker.registerFunction("NectarGetPosts", ApiaryConfig.postgres, NectarGetPosts::new);
        apiaryWorker.startServing();

        this.client = new ApiaryWorkerClient("localhost");
    }

    @GetMapping("/")
    public RedirectView index(Model model) {
        return new RedirectView("/home");
    }

    @GetMapping("/home")
    public String home(Model model) {
        return "home";
    }

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("registration", new Credentials());
        return "registration";
    }

    @PostMapping("/registration")
    public String registrationSubmit(@ModelAttribute Credentials credentials, Model model) throws IOException {
        int success = client.executeFunction("NectarRegister", credentials.getUsername(), credentials.getPassword()).getInt();
        if (success != 0) {
            return "redirect:/home";
        }
        model.addAttribute("registration", credentials);
        return "registration_result";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("login", new Credentials());
        return "login";
    }

    @PostMapping("/login")
    public RedirectView loginSubmit(@ModelAttribute Credentials credentials, @ModelAttribute("logincredentials") Credentials logincredentials, RedirectAttributes attributes) throws InvalidProtocolBufferException {
        int success = client.executeFunction("NectarLogin", credentials.getUsername(), credentials.getPassword()).getInt();
        if (success == 0) { // Login successful.
            logincredentials.setUsername(credentials.getUsername());
            logincredentials.setPassword(credentials.getPassword());
            // Ensure credentials are saved across page reloads.
            attributes.addFlashAttribute("logincredentials", logincredentials);
            return new RedirectView("/timeline");
        } else { // Login failed.
            return new RedirectView("/home");
        }
    }

    @RequestMapping("/logout")
    public String logoutSession(@ModelAttribute Credentials credentials, @ModelAttribute("logincredentials") Credentials logincredentials, Model model,
                                HttpServletRequest request,
                                SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/home";
    }

    private List<WebPost> findUserPosts(String username) throws InvalidProtocolBufferException {
        List<WebPost> postList = new ArrayList<>();
        String[] posts = client.executeFunction("NectarGetPosts", username).getStringArray();
        for (String post: posts) {
            WebPost webPost = new WebPost();
            JSONObject obj = (JSONObject) JSONValue.parse(post);
            webPost.setSender((String) obj.get("Sender"));
            webPost.setPostText((String) obj.get("PostText"));
            postList.add(webPost);
        }
        return postList;
    }

    @GetMapping("/timeline")
    public String timeline(Model model, @ModelAttribute("logincredentials") Credentials logincredentials) throws InvalidProtocolBufferException {
        if (logincredentials.getUsername() != null) {
            model.addAttribute("login", logincredentials);
            List<WebPost> postlist = findUserPosts(logincredentials.getUsername());
            model.addAttribute("timelinelist", postlist);
            model.addAttribute("addpost", new WebPost());
            return "timeline";
        } else {
            return "redirect:/home";
        }
    }

    @PostMapping("/timeline")
    public RedirectView timelinePostSubmit(@ModelAttribute WebPost webPost, @ModelAttribute("logincredentials") Credentials logincredentials) throws InvalidProtocolBufferException {
        if (logincredentials.getUsername() == null) {
            return new RedirectView("/home");
        }
        client.executeFunction("NectarAddPost", logincredentials.getUsername(), webPost.getReceiver(), webPost.getPostText());
        return new RedirectView("/timeline");
    }

    @ModelAttribute("logincredentials")
    public Credentials logincredentials() {
        return new Credentials();
    }

    @Autowired
    private ETLService etlService;

    // Connect to etl.html file
    @GetMapping("/etl")
    public String etlPage() {
        return "etl";  
    }

    // This endpoint is called when the ETL button is clicked
    @GetMapping(value = "/start-etl", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> startETLProcess() {
        try {
            // Start the ETL process
            etlService.runETL();
            // Return success message
            return ResponseEntity.status(HttpStatus.OK).body("ETL completed!");
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during ETL process");
        }
    }

    // Clickhouse
    // Inject the ClickHouseService into the controller
    @Autowired
    private ClickHouseService clickHouseService;


    @GetMapping("/select-tables")
    public String selectTablesPage() {
        return "select_tables"; // This should match the HTML filename without the .html extension
    }

    @GetMapping("/view-table")
    public String viewTablePage() {
        return "view_table"; // This should match the HTML filename without the .html extension
    }

    // This endpoint is called to populate databases
    @GetMapping(value = "/get-databases", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> fetchDB() {
        try {
            // Get the list of databases
            List<String> databases = clickHouseService.getDatabases();
            // Return the list of databases
            return ResponseEntity.ok(databases);
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // This endpoint fetches tables for a specific database
    @GetMapping(value = "/get-tables", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> fetchTables(@RequestParam String database) {
        try {
            // Get the list of tables for the specified database
            List<String> tables = clickHouseService.getTables(database);
            // Return the list of tables
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // This endpoint fetches contents of a specific table
    @GetMapping(value = "/get-table-contents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> fetchTableContents(@RequestParam String database, @RequestParam String table) {
        try {
            // Get the contents of the specified table
            List<String> tableContents = clickHouseService.getTableContents(database, table);
            // Return the list of contents
            return ResponseEntity.ok(tableContents);
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
