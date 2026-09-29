package com.example.data.local

import com.example.data.model.ItemStatus
import com.example.data.model.Severity
import com.example.data.model.TechnicianUser

object SampleData {
    val USERS = listOf(
        TechnicianUser("user_raja", "Raja", "Lead Technician", "RJ"),
        TechnicianUser("user_tamil", "Tamil", "Field Technician", "TM"),
        TechnicianUser("user_vignesh", "Vignesh", "Safety Supervisor", "VG"),
        TechnicianUser("user_perarasu", "Perarasu", "Operations Manager", "PR")
    )

    fun getInitialInspections(): List<InspectionEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            InspectionEntity(
                id = "insp_factory_a",
                title = "Factory Block A - Generator Turbine #1",
                facility = "Factory Block A",
                status = "IN_PROGRESS",
                assignedTechnician = "Raja",
                technicianRole = "Lead Technician",
                scheduledDate = "Today, 09:00 AM",
                updatedAt = now - 1800000,
                progressPercent = 50,
                totalItems = 6,
                completedItems = 3,
                failedItems = 1,
                hasConflict = false
            ),
            InspectionEntity(
                id = "insp_solar_north",
                title = "Solar Plant North - Substation Grid Inverters",
                facility = "Solar Plant North",
                status = "PENDING_REVIEW",
                assignedTechnician = "Tamil",
                technicianRole = "Field Technician",
                scheduledDate = "Today, 11:30 AM",
                updatedAt = now - 3600000,
                progressPercent = 83,
                totalItems = 6,
                completedItems = 5,
                failedItems = 0,
                hasConflict = false
            ),
            InspectionEntity(
                id = "insp_warehouse_b",
                title = "Warehouse B - High-Bay Conveyor & HVAC #3",
                facility = "Warehouse B",
                status = "IN_PROGRESS",
                assignedTechnician = "Raja",
                technicianRole = "Lead Technician",
                scheduledDate = "Yesterday, 02:00 PM",
                updatedAt = now - 86400000,
                progressPercent = 33,
                totalItems = 6,
                completedItems = 2,
                failedItems = 0,
                hasConflict = false
            ),
            InspectionEntity(
                id = "insp_prod_line_2",
                title = "Production Line 2 - Robotic Arm Actuators",
                facility = "Production Line 2",
                status = "IN_PROGRESS",
                assignedTechnician = "Tamil",
                technicianRole = "Field Technician",
                scheduledDate = "Tomorrow, 08:30 AM",
                updatedAt = now - 7200000,
                progressPercent = 16,
                totalItems = 6,
                completedItems = 1,
                failedItems = 0,
                hasConflict = false
            ),
            InspectionEntity(
                id = "insp_elec_room_4",
                title = "Electrical Room 4 - Switchgear & Transformer 400V",
                facility = "Electrical Room 4",
                status = "COMPLETED",
                assignedTechnician = "Vignesh",
                technicianRole = "Safety Supervisor",
                scheduledDate = "2 days ago",
                updatedAt = now - 172800000,
                progressPercent = 100,
                totalItems = 6,
                completedItems = 6,
                failedItems = 0,
                hasConflict = false
            )
        )
    }

    fun getInitialChecklistItems(): List<ChecklistItemEntity> {
        val now = System.currentTimeMillis()
        val allItems = mutableListOf<ChecklistItemEntity>()

        // Factory Block A Checklist
        val factoryAItems = listOf(
            ChecklistItemEntity(
                id = "item_f_a_1",
                inspectionId = "insp_factory_a",
                title = "Motor condition",
                category = "Mechanical Integrity",
                status = ItemStatus.PASS.name,
                severity = Severity.LOW.name,
                notes = "Minor surface vibration within ISO 10816 threshold (1.8 mm/s). Bearings smooth.",
                recommendation = "Re-check grease fitting at 250 operational hours.",
                photosJson = "motor_housing_front.jpg",
                lastModifiedBy = "Raja",
                lastModifiedAt = now - 2400000,
                deviceId = "FIELD-TAB-01",
                version = 1
            ),
            ChecklistItemEntity(
                id = "item_f_a_2",
                inspectionId = "insp_factory_a",
                title = "Oil level",
                category = "Lubrication & Fluid",
                status = ItemStatus.PASS.name,
                severity = Severity.NONE.name,
                notes = "Sight glass indicates 72% reservoir capacity. Color amber, no particulates.",
                recommendation = "Normal operating level.",
                photosJson = "oil_sight_glass.jpg",
                lastModifiedBy = "Raja",
                lastModifiedAt = now - 2100000,
                deviceId = "FIELD-TAB-01",
                version = 1
            ),
            ChecklistItemEntity(
                id = "item_f_a_3",
                inspectionId = "insp_factory_a",
                title = "Temperature",
                category = "Thermal Monitoring",
                status = ItemStatus.FAIL.name,
                severity = Severity.HIGH.name,
                notes = "Thermal scan reading 88.4°C at stator core hotspot. Normal limit is 75°C.",
                recommendation = "Inspect internal cooling fan duct for particulate blockage.",
                photosJson = "thermal_scan_motor.jpg",
                lastModifiedBy = "Raja",
                lastModifiedAt = now - 1900000,
                deviceId = "FIELD-TAB-01",
                version = 1
            ),
            ChecklistItemEntity(
                id = "item_f_a_4",
                inspectionId = "insp_factory_a",
                title = "Emergency switch",
                category = "Safety Interlocks",
                status = ItemStatus.UNCHECKED.name,
                severity = Severity.NONE.name,
                notes = "",
                recommendation = "",
                photosJson = "",
                lastModifiedBy = "Raja",
                lastModifiedAt = now - 3600000,
                deviceId = "FIELD-TAB-01",
                version = 1
            ),
            ChecklistItemEntity(
                id = "item_f_a_5",
                inspectionId = "insp_factory_a",
                title = "Panel cover",
                category = "Physical Enclosure",
                status = ItemStatus.UNCHECKED.name,
                severity = Severity.NONE.name,
                notes = "",
                recommendation = "",
                photosJson = "",
                lastModifiedBy = "Raja",
                lastModifiedAt = now - 3600000,
                deviceId = "FIELD-TAB-01",
                version = 1
            ),
            ChecklistItemEntity(
                id = "item_f_a_6",
                inspectionId = "insp_factory_a",
                title = "Safety equipment",
                category = "PPE & Extinguishers",
                status = ItemStatus.UNCHECKED.name,
                severity = Severity.NONE.name,
                notes = "",
                recommendation = "",
                photosJson = "",
                lastModifiedBy = "Raja",
                lastModifiedAt = now - 3600000,
                deviceId = "FIELD-TAB-01",
                version = 1
            )
        )
        allItems.addAll(factoryAItems)

        // Seed items for other inspections
        val otherInspections = listOf("insp_solar_north", "insp_warehouse_b", "insp_prod_line_2", "insp_elec_room_4")
        val standardTitles = listOf(
            "Motor condition" to "Mechanical Integrity",
            "Oil level" to "Lubrication & Fluid",
            "Temperature" to "Thermal Monitoring",
            "Emergency switch" to "Safety Interlocks",
            "Panel cover" to "Physical Enclosure",
            "Safety equipment" to "PPE & Extinguishers"
        )

        for (inspId in otherInspections) {
            standardTitles.forEachIndexed { index, pair ->
                val isCompleted = inspId == "insp_elec_room_4" || (inspId == "insp_solar_north" && index < 5)
                val status = if (isCompleted) ItemStatus.PASS.name else ItemStatus.UNCHECKED.name
                allItems.add(
                    ChecklistItemEntity(
                        id = "item_${inspId}_${index + 1}",
                        inspectionId = inspId,
                        title = pair.first,
                        category = pair.second,
                        status = status,
                        severity = Severity.NONE.name,
                        notes = if (isCompleted) "Verified within standard operating parameters." else "",
                        recommendation = "",
                        photosJson = "",
                        lastModifiedBy = if (inspId == "insp_elec_room_4") "Vignesh" else "Tamil",
                        lastModifiedAt = now - (index * 300000),
                        deviceId = "FIELD-UNIT-02",
                        version = 1
                    )
                )
            }
        }

        return allItems
    }

    fun getInitialAuditEvents(): List<AuditEventEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            AuditEventEntity(
                inspectionId = "insp_factory_a",
                userName = "Raja",
                userRole = "Lead Technician",
                action = "CHECKLIST_UPDATED",
                targetItem = "Temperature",
                oldValue = "UNCHECKED",
                newValue = "FAIL (88.4°C)",
                deviceId = "FIELD-TAB-01",
                timestamp = now - 1900000,
                details = "Flagged thermal anomaly at stator core hotspot."
            ),
            AuditEventEntity(
                inspectionId = "insp_factory_a",
                userName = "Raja",
                userRole = "Lead Technician",
                action = "PHOTO_ATTACHED",
                targetItem = "Temperature",
                oldValue = "None",
                newValue = "thermal_scan_motor.jpg",
                deviceId = "FIELD-TAB-01",
                timestamp = now - 2000000,
                details = "High-resolution thermal camera image attached (2.4MB)."
            ),
            AuditEventEntity(
                inspectionId = "insp_factory_a",
                userName = "Raja",
                userRole = "Lead Technician",
                action = "CHECKLIST_UPDATED",
                targetItem = "Oil level",
                oldValue = "UNCHECKED",
                newValue = "PASS (72%)",
                deviceId = "FIELD-TAB-01",
                timestamp = now - 2100000,
                details = "Fluid level verified within safe operational threshold."
            ),
            AuditEventEntity(
                inspectionId = "insp_solar_north",
                userName = "Tamil",
                userRole = "Field Technician",
                action = "INSPECTION_SUBMITTED",
                targetItem = "Full Audit",
                oldValue = "IN_PROGRESS",
                newValue = "PENDING_REVIEW",
                deviceId = "FIELD-UNIT-02",
                timestamp = now - 3600000,
                details = "All primary inverter arrays checked and submitted for supervisor sign-off."
            ),
            AuditEventEntity(
                inspectionId = "insp_elec_room_4",
                userName = "Vignesh",
                userRole = "Safety Supervisor",
                action = "AUDIT_SIGNED",
                targetItem = "Compliance Certification",
                oldValue = "PENDING",
                newValue = "COMPLETED",
                deviceId = "MGR-WORKSTATION-01",
                timestamp = now - 172800000,
                details = "Bi-weekly 400V arc-flash and switchgear clearance approved."
            )
        )
    }
}
