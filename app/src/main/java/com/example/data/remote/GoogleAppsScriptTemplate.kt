package com.example.data.remote

object GoogleAppsScriptTemplate {

    val SCHEMA_DOCUMENTATION = """
    =============================================================================
    BALAJI AIR CONDITIONERS - GOOGLE SHEETS DATABASE SCHEMA (6 TABS)
    =============================================================================
    
    Tab 1: "Users"
    Columns:
      A: Phone (String, Primary Key, 10 digits)
      B: Name (String)
      C: Address (String)
      D: Area (String)
      E: Role (String: "CUSTOMER" or "ADMIN")
      F: CreatedAt (Date / Timestamp)
      
    Tab 2: "Bookings"
    Columns:
      A: ID (String, Primary Key e.g. "BK-1001")
      B: CustomerName (String)
      C: CustomerPhone (String)
      D: Address (String)
      E: Area (String)
      F: ServiceName (String)
      G: Units (Number)
      H: ModelType (String: "Split AC", "Window AC", "Inverter AC", "Single Door Fridge", etc.)
      I: IssueNotes (String)
      J: CouponCode (String)
      K: DiscountAmount (Number)
      L: BillAmount (Number)
      M: SparePartsCharge (Number)
      N: Status (String: "PENDING", "TECHNICIAN_ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED")
      O: TechnicianName (String: "Sanjay Prajapati" / "Sandip Prajapati")
      P: CreatedAt (Date / Timestamp)

    Tab 3: "Services"
    Columns:
      A: ID (String, Primary Key)
      B: Name (String)
      C: Description (String)
      D: EstimatedTime (String)
      E: StartingPrice (Number)
      F: IconType (String)
      G: IsActive (Boolean: TRUE/FALSE)

    Tab 4: "Offers"
    Columns:
      A: Code (String, Primary Key e.g. "BALAJI100")
      B: Title (String)
      C: DiscountPercent (Number)
      D: MaxDiscount (Number)
      E: Description (String)
      F: IsActive (Boolean: TRUE/FALSE)

    Tab 5: "Gallery"
    Columns:
      A: ID (String)
      B: Title (String)
      C: Category (String: "AC Jet Wash", "Installation", "Fridge Repair", "Gas Charging", "PCB & Compressor")
      D: Description (String)
      E: ImageUrl (String)
      F: Tag (String)

    Tab 6: "Reviews"
    Columns:
      A: ID (String)
      B: CustomerName (String)
      C: Rating (Number e.g. 5.0)
      D: DateText (String)
      E: Comment (String)
      F: ServiceUsed (String)
      G: OwnerReply (String)
      H: OwnerReplyBy (String: "Balaji Air Conditioners")
    =============================================================================
    """.trimIndent()

    val COMPLETE_APPS_SCRIPT_CODE = """
/**
 * BALAJI AIR CONDITIONERS - GOOGLE APPS SCRIPT WEB APP API
 * Manages all CRUD operations for Balaji Air Conditioners AC & Fridge Service.
 *
 * HOW TO DEPLOY:
 * 1. Open your Google Sheet.
 * 2. Click Extensions > Apps Script.
 * 3. Delete any code and paste this entire script.
 * 4. Click 'Deploy' > 'New deployment'.
 * 5. Select type: 'Web app'.
 * 6. Execute as: 'Me' (your Google account).
 * 7. Who has access: 'Anyone'.
 * 8. Copy the Web app URL and paste it into the Balaji AC App Admin Panel!
 */

function doGet(e) {
  var action = (e && e.parameter && e.parameter.action) ? e.parameter.action : "ping";
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  ensureSheetsSetup(ss);

  try {
    switch (action) {
      case "ping":
        return jsonResponse({ status: "success", message: "Balaji Air Conditioners API Online!" });

      case "getServices":
        return jsonResponse({ status: "success", data: getSheetDataAsObjects(ss.getSheetByName("Services")) });

      case "getBookings":
        var phone = e.parameter.phone;
        var allBookings = getSheetDataAsObjects(ss.getSheetByName("Bookings"));
        if (phone) {
          allBookings = allBookings.filter(function(b) { return String(b.CustomerPhone) === String(phone); });
        }
        return jsonResponse({ status: "success", data: allBookings });

      case "getUsers":
        return jsonResponse({ status: "success", data: getSheetDataAsObjects(ss.getSheetByName("Users")) });

      case "getOffers":
        return jsonResponse({ status: "success", data: getSheetDataAsObjects(ss.getSheetByName("Offers")) });

      case "getGallery":
        return jsonResponse({ status: "success", data: getSheetDataAsObjects(ss.getSheetByName("Gallery")) });

      case "getReviews":
        return jsonResponse({ status: "success", data: getSheetDataAsObjects(ss.getSheetByName("Reviews")) });

      default:
        return jsonResponse({ status: "error", message: "Unknown action: " + action });
    }
  } catch (err) {
    return jsonResponse({ status: "error", error: err.toString() });
  }
}

function doPost(e) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  ensureSheetsSetup(ss);

  try {
    var body = {};
    if (e.postData && e.postData.contents) {
      body = JSON.parse(e.postData.contents);
    }
    var action = body.action || (e.parameter ? e.parameter.action : "");

    switch (action) {
      // 1. Check or Register User
      case "syncUser":
        var usersSheet = ss.getSheetByName("Users");
        var existing = findRowIndexByColumn(usersSheet, 1, body.phone);
        if (existing > 1) {
          // Update user
          usersSheet.getRange(existing, 2).setValue(body.name || "");
          usersSheet.getRange(existing, 3).setValue(body.address || "");
          usersSheet.getRange(existing, 4).setValue(body.area || "");
          return jsonResponse({ status: "success", message: "User updated", isNew: false });
        } else {
          // Append new customer
          usersSheet.appendRow([
            String(body.phone),
            body.name || "Customer",
            body.address || "",
            body.area || "",
            body.role || "CUSTOMER",
            new Date()
          ]);
          return jsonResponse({ status: "success", message: "New user registered", isNew: true });
        }

      // 2. Add Booking
      case "addBooking":
        var bookingsSheet = ss.getSheetByName("Bookings");
        var bookingId = "BK-" + Math.floor(100000 + Math.random() * 900000);
        bookingsSheet.appendRow([
          bookingId,
          body.customerName || "",
          String(body.customerPhone || ""),
          body.address || "",
          body.area || "",
          body.serviceName || "",
          Number(body.units || 1),
          body.modelType || "Split AC",
          body.issueNotes || "",
          body.couponCode || "",
          Number(body.discountAmount || 0),
          Number(body.billAmount || 0),
          Number(body.sparePartsCharge || 0),
          "PENDING",
          body.technicianName || "Sanjay Prajapati",
          new Date()
        ]);

        // Send instant notification to Sanjay Prajapati
        try {
          var ownerEmail = "prajapatisanjay70074@gmail.com";
          var emailSubject = "🚨 New Balaji AC Appointment: " + (body.serviceName || "Service") + " (" + bookingId + ")";
          var emailBody = "Namaste Sanjay & Sandip Prajapati ji,\n\n" +
                          "App se nayi appointment book hui hai:\n\n" +
                          "• Booking ID: " + bookingId + "\n" +
                          "• Customer Name: " + (body.customerName || "N/A") + "\n" +
                          "• Mobile: " + (body.customerPhone || "N/A") + "\n" +
                          "• Address: " + (body.address || "N/A") + ", " + (body.area || "") + "\n" +
                          "• Service: " + (body.serviceName || "") + " (" + (body.units || 1) + " Unit, " + (body.modelType || "") + ")\n" +
                          "• Problem / Notes: " + (body.issueNotes || "None") + "\n" +
                          "• Coupon Applied: " + (body.couponCode || "None") + "\n" +
                          "• Status: PENDING\n\n" +
                          "Direct WhatsApp Contact: https://wa.me/91" + body.customerPhone + "\n\n" +
                          "- Balaji Air Conditioners Automated Booking System";
          MailApp.sendEmail(ownerEmail, emailSubject, emailBody);
        } catch(notifyErr) {}

        return jsonResponse({ status: "success", bookingId: bookingId });

      // 3. Update Booking Status / Invoice
      case "updateBookingStatus":
        var bSheet = ss.getSheetByName("Bookings");
        var bRow = findRowIndexByColumn(bSheet, 1, body.bookingId);
        if (bRow > 1) {
          if (body.status) bSheet.getRange(bRow, 14).setValue(body.status);
          if (body.billAmount !== undefined) bSheet.getRange(bRow, 12).setValue(Number(body.billAmount));
          if (body.sparePartsCharge !== undefined) bSheet.getRange(bRow, 13).setValue(Number(body.sparePartsCharge));
          if (body.technicianName) bSheet.getRange(bRow, 15).setValue(body.technicianName);
          return jsonResponse({ status: "success", message: "Booking updated" });
        }
        return jsonResponse({ status: "error", message: "Booking not found" });

      // 4. Manage Services (CRUD)
      case "saveService":
        var sSheet = ss.getSheetByName("Services");
        var sRow = findRowIndexByColumn(sSheet, 1, body.id);
        if (sRow > 1) {
          sSheet.getRange(sRow, 2).setValue(body.name);
          sSheet.getRange(sRow, 3).setValue(body.description);
          sSheet.getRange(sRow, 4).setValue(body.estimatedTime);
          sSheet.getRange(sRow, 5).setValue(Number(body.startingPrice || 0));
          sSheet.getRange(sRow, 6).setValue(body.iconType || "AC_SERVICE");
          sSheet.getRange(sRow, 7).setValue(body.isActive !== false);
          return jsonResponse({ status: "success", message: "Service updated" });
        } else {
          var newId = body.id || ("SRV-" + new Date().getTime());
          sSheet.appendRow([
            newId,
            body.name,
            body.description,
            body.estimatedTime,
            Number(body.startingPrice || 0),
            body.iconType || "AC_SERVICE",
            body.isActive !== false
          ]);
          return jsonResponse({ status: "success", id: newId });
        }

      case "deleteService":
        var sDelSheet = ss.getSheetByName("Services");
        var sDelRow = findRowIndexByColumn(sDelSheet, 1, body.id);
        if (sDelRow > 1) {
          sDelSheet.deleteRow(sDelRow);
          return jsonResponse({ status: "success", message: "Service deleted" });
        }
        return jsonResponse({ status: "error", message: "Service not found" });

      // 5. Delete Fake/Unwanted User
      case "deleteUser":
        var uSheet = ss.getSheetByName("Users");
        var uRow = findRowIndexByColumn(uSheet, 1, body.phone);
        if (uRow > 1) {
          uSheet.deleteRow(uRow);
          return jsonResponse({ status: "success", message: "User deleted" });
        }
        return jsonResponse({ status: "error", message: "User not found" });

      // 6. Manage Reviews & Owner Reply
      case "addOwnerReply":
        var rSheet = ss.getSheetByName("Reviews");
        var rRow = findRowIndexByColumn(rSheet, 1, body.id);
        if (rRow > 1) {
          rSheet.getRange(rRow, 7).setValue(body.ownerReply);
          rSheet.getRange(rRow, 8).setValue(body.ownerReplyBy || "Balaji Air Conditioners");
          return jsonResponse({ status: "success", message: "Official reply posted" });
        }
        return jsonResponse({ status: "error", message: "Review not found" });

      case "deleteReview":
        var rDelSheet = ss.getSheetByName("Reviews");
        var rDelRow = findRowIndexByColumn(rDelSheet, 1, body.id);
        if (rDelRow > 1) {
          rDelSheet.deleteRow(rDelRow);
          return jsonResponse({ status: "success", message: "Review deleted" });
        }
        return jsonResponse({ status: "error", message: "Review not found" });

      default:
        return jsonResponse({ status: "error", message: "Unsupported POST action: " + action });
    }
  } catch (err) {
    return jsonResponse({ status: "error", error: err.toString() });
  }
}

// Helpers
function jsonResponse(data) {
  return ContentService.createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}

function getSheetDataAsObjects(sheet) {
  if (!sheet) return [];
  var values = sheet.getDataRange().getValues();
  if (values.length <= 1) return [];
  var headers = values[0];
  var result = [];
  for (var i = 1; i < values.length; i++) {
    var row = values[i];
    var obj = {};
    for (var j = 0; j < headers.length; j++) {
      obj[headers[j]] = row[j];
    }
    result.push(obj);
  }
  return result;
}

function findRowIndexByColumn(sheet, colIndex, value) {
  var values = sheet.getDataRange().getValues();
  for (var i = 1; i < values.length; i++) {
    if (String(values[i][colIndex - 1]) === String(value)) {
      return i + 1; // 1-indexed row number
    }
  }
  return -1;
}

function ensureSheetsSetup(ss) {
  var sheets = {
    "Users": ["Phone", "Name", "Address", "Area", "Role", "CreatedAt"],
    "Bookings": ["ID", "CustomerName", "CustomerPhone", "Address", "Area", "ServiceName", "Units", "ModelType", "IssueNotes", "CouponCode", "DiscountAmount", "BillAmount", "SparePartsCharge", "Status", "TechnicianName", "CreatedAt"],
    "Services": ["ID", "Name", "Description", "EstimatedTime", "StartingPrice", "IconType", "IsActive"],
    "Offers": ["Code", "Title", "DiscountPercent", "MaxDiscount", "Description", "IsActive"],
    "Gallery": ["ID", "Title", "Category", "Description", "ImageUrl", "Tag"],
    "Reviews": ["ID", "CustomerName", "Rating", "DateText", "Comment", "ServiceUsed", "OwnerReply", "OwnerReplyBy"]
  };

  for (var name in sheets) {
    var sheet = ss.getSheetByName(name);
    if (!sheet) {
      sheet = ss.insertSheet(name);
      sheet.appendRow(sheets[name]);
      sheet.getRange(1, 1, 1, sheets[name].length).setFontWeight("bold").setBackground("#005F73").setFontColor("#FFFFFF");
    }
  }
}
    """.trimIndent()
}
