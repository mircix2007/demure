package com.wardrobe.digital_wardrobe;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.wardrobe.digital_wardrobe.ClothingItem;
import com.wardrobe.digital_wardrobe.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@Controller
public class WardrobeController {

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/")
    public String dashboard(HttpSession session,
                            @RequestParam(value = "cat", defaultValue = "All") String category,
                            Model model) throws Exception {

        // Get user from session (set during login)
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login"; // Force login

        model.addAttribute("items", firebaseService.getItems(uid, category));
        model.addAttribute("selectedCat", category);
        return "dashboard";
    }

    @PostMapping("/upload")
    @ResponseBody // Tell Spring to return data, not a view template
    public ClothingItem upload(HttpSession session,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam("name") String name,
                               @RequestParam("category") String category) throws Exception {

        String uid = (String) session.getAttribute("uid");
        String publicUrl = firebaseService.uploadAndGetUrl(file);

        ClothingItem item = new ClothingItem(uid, name, category, publicUrl);
        firebaseService.saveItem(item); // This now sets the ID inside the object

        return item; // Returns the item as JSON to the browser
    }


    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/set-session")
    @ResponseBody
    public ResponseEntity<String> setSession(@RequestBody Map<String, String> payload, HttpSession session) {
        String uid = payload.get("uid");
        if (uid != null) {
            session.setAttribute("uid", uid);
            return ResponseEntity.ok("Session set");
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/delete-selected")
    public String deleteSelected(@RequestParam(value = "selectedIds", required = false) List<String> ids) throws Exception {
        if (ids != null) {
            Firestore db = FirestoreClient.getFirestore();
            for (String id : ids) {
                // Get the document to find the imageUrl before it's gone
                DocumentSnapshot doc = db.collection("clothes").document(id).get().get();
                if (doc.exists()) {
                    String imageUrl = doc.getString("imageUrl");
                    firebaseService.deleteItem(id, imageUrl);
                }
            }
        }
        return "redirect:/";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Destroys the "uid" stored in the session
        return "redirect:/login";
    }
}