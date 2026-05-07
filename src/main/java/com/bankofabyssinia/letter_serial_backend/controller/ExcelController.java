// package com.bankofabyssinia.letter_serial_system.controller;

// import com.bankofabyssinia.letter_serial_system.dto.UserDTO;
// import com.bankofabyssinia.letter_serial_system.service.ExcelService;
// import com.bankofabyssinia.letter_serial_system.service.UserService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/excel")
// public class ExcelController {
//     private static final Logger log = LoggerFactory.getLogger(ExcelController.class);

//     private final ExcelService excelService;
//     private final UserService userService;
//     private final UserController userController;

//     public ExcelController(ExcelService excelService, UserService userService,
//             UserController userController) {
//         this.excelService = excelService;
//         this.userService = userService;
//         this.userController = userController;
//     }

//     @PostMapping("/users/import-with-auto-create")
//     public ResponseEntity<?> importUsersWithAutoCreate(@RequestParam("file") MultipartFile file) {
//         try {
//             if (file.isEmpty()) {
//                 return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "File is empty"));
//             }

//             String originalFilename = file.getOriginalFilename();
//             if (originalFilename == null || !originalFilename.endsWith(".xlsx")) {
//                 return ResponseEntity.badRequest()
//                         .body(Map.of("status", "error", "message", "Only .xlsx files are supported"));
//             }

//             // Parse Excel file
//             List<ExcelService.UserImportDTO> importedUsers = excelService.parseUsersExcel(file.getInputStream());

//             if (importedUsers.isEmpty()) {
//                 return ResponseEntity.badRequest()
//                         .body(Map.of("status", "error", "message", "No valid users found in the file"));
//             }

//             // Process imported users with auto-creation of districts and branches
//             List<ExcelService.UserImportResultDTO> processedUsers = excelService.processUserImport(importedUsers);

//             // Create users using UserController
//             List<Map<String, Object>> results = new ArrayList<>();
//             int successCount = 0;
//             int errorCount = 0;

//             for (ExcelService.UserImportResultDTO processedUser : processedUsers) {
//                 Map<String, Object> result = new HashMap<>();
//                 result.put("name", processedUser.getOriginalData().getName());
//                 result.put("email", processedUser.getOriginalData().getEmail());
//                 result.put("district", processedUser.getOriginalData().getDistrictName()); // Changed from office
//                 result.put("branch", processedUser.getOriginalData().getBranchName()); // Changed from department

//                 if (processedUser.isSuccess()) {
//                     try {
//                         ResponseEntity<?> response = userController.createUser(processedUser.getUserDTO());
//                         if (response.getStatusCode().is2xxSuccessful()) {
//                             successCount++;
//                             result.put("status", "success");
//                             result.put("message", "User created successfully");
//                         } else {
//                             errorCount++;
//                             result.put("status", "error");
//                             // Safely handle response body
//                             Object responseBody = response.getBody();
//                             String errorMessage = responseBody != null ? responseBody.toString() : "Unknown error";
//                             result.put("message", "Failed to create user: " + errorMessage);
//                         }
//                     } catch (Exception e) {
//                         errorCount++;
//                         result.put("status", "error");
//                         result.put("message", "Error creating user: " + e.getMessage());
//                     }
//                 } else {
//                     errorCount++;
//                     result.put("status", "error");
//                     result.put("message", processedUser.getMessage());
//                 }

//                 results.add(result);
//             }

//             Map<String, Object> response = new HashMap<>();
//             response.put("summary", Map.of(
//                     "total", importedUsers.size(),
//                     "successful", successCount,
//                     "failed", errorCount));
//             response.put("details", results);

//             return ResponseEntity.ok(response);

//         } catch (IOException e) {
//             log.error("Error importing users from Excel: {}", e.getMessage(), e);
//             return ResponseEntity.internalServerError()
//                     .body(Map.of("status", "error", "message", "Error processing file: " + e.getMessage()));
//         } catch (Exception e) {
//             log.error("Unexpected error during import: {}", e.getMessage(), e);
//             return ResponseEntity.internalServerError()
//                     .body(Map.of("status", "error", "message", "Unexpected error: " + e.getMessage()));
//         }
//     }

//     @GetMapping("/users/template")
//     public ResponseEntity<byte[]> downloadTemplate() throws IOException {
//         List<UserDTO> emptyList = List.of(); // Empty list for template

//         byte[] excelData = excelService.generateUsersExcel(emptyList);

//         return ResponseEntity.ok()
//                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_template.xlsx")
//                 .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//                 .body(excelData);
//     }

//     @GetMapping("/users/export")
//     public ResponseEntity<byte[]> exportUsers() throws IOException {
//         // Changed from getAllUsersForExport() to getAllUsers()
//         List<UserDTO> users = userService.getAllUsers();

//         byte[] excelData = excelService.generateUsersExcel(users);

//         return ResponseEntity.ok()
//                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_export.xlsx")
//                 .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//                 .body(excelData);
//     }

//     @PostMapping("/users/import")
//     public ResponseEntity<?> importUsers(@RequestParam("file") MultipartFile file) {
//         try {
//             if (file.isEmpty()) {
//                 return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "File is empty"));
//             }

//             String originalFilename = file.getOriginalFilename();
//             if (originalFilename == null || !originalFilename.endsWith(".xlsx")) {
//                 return ResponseEntity.badRequest()
//                         .body(Map.of("status", "error", "message", "Only .xlsx files are supported"));
//             }

//             List<ExcelService.UserImportDTO> importedUsers = excelService.parseUsersExcel(file.getInputStream());

//             if (importedUsers.isEmpty()) {
//                 return ResponseEntity.badRequest()
//                         .body(Map.of("status", "error", "message", "No valid users found in the file"));
//             }

//             // Process imported users
//             List<Map<String, Object>> results = new ArrayList<>();
//             int successCount = 0;
//             int errorCount = 0;

//             for (ExcelService.UserImportDTO importedUser : importedUsers) {
//                 Map<String, Object> result = new HashMap<>();
//                 result.put("name", importedUser.getName());
//                 result.put("email", importedUser.getEmail());

//                 try {
//                     // Convert to UserDTO and create user
//                     UserDTO userDTO = new UserDTO();
//                     userDTO.setName(importedUser.getName());
//                     userDTO.setEmail(importedUser.getEmail());
//                     userDTO.setRole(importedUser.getRole() != null ? importedUser.getRole() : "ROLE_SECRETARY");
//                     userDTO.setIsActive(true);
//                     userDTO.setPassword("Temp123!"); // Default password, user should change

//                     // Note: This will fail without districtId and branchId
//                     // Use the new import-with-auto-create endpoint instead
//                     ResponseEntity<?> response = userController.createUser(userDTO);
//                     if (response.getStatusCode().is2xxSuccessful()) {
//                         successCount++;
//                         result.put("status", "success");
//                         result.put("message", "User created successfully");
//                     } else {
//                         errorCount++;
//                         result.put("status", "error");
//                         Object responseBody = response.getBody();
//                         String errorMessage = responseBody != null ? responseBody.toString() : "Unknown error";
//                         result.put("message", "Failed to create user: " + errorMessage);
//                     }
//                 } catch (Exception e) {
//                     errorCount++;
//                     result.put("status", "error");
//                     result.put("message", "Error with user: " + e.getMessage());
//                 }

//                 results.add(result);
//             }

//             Map<String, Object> response = new HashMap<>();
//             response.put("summary", Map.of(
//                     "total", importedUsers.size(),
//                     "successful", successCount,
//                     "failed", errorCount));
//             response.put("details", results);

//             return ResponseEntity.ok(response);

//         } catch (IOException e) {
//             log.error("Error importing users from Excel: {}", e.getMessage(), e);
//             return ResponseEntity.internalServerError()
//                     .body(Map.of("status", "error", "message", "Error processing file: " + e.getMessage()));
//         } catch (Exception e) {
//             log.error("Unexpected error during import: {}", e.getMessage(), e);
//             return ResponseEntity.internalServerError()
//                     .body(Map.of("status", "error", "message", "Unexpected error: " + e.getMessage()));
//         }
//     }
// }