package com.multivendor.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SlotDtos {

    public static class CreateSlotRequest {
        @NotNull
        private Long serviceId;

        @NotNull
        private LocalDateTime startTime;

        @NotNull
        private LocalDateTime endTime;

        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }

    public static class BatchSlotGeneratorRequest {
        @NotNull
        private Long serviceId;

        @NotNull
        private LocalDateTime startDate;

        @NotNull
        private Integer daysCount = 7;

        @NotNull
        private Integer startHour = 9; // e.g. 9 AM

        @NotNull
        private Integer endHour = 17; // e.g. 5 PM

        @NotNull
        private Integer slotDurationMinutes = 60;

        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public Integer getDaysCount() { return daysCount; }
        public void setDaysCount(Integer daysCount) { this.daysCount = daysCount; }

        public Integer getStartHour() { return startHour; }
        public void setStartHour(Integer startHour) { this.startHour = startHour; }

        public Integer getEndHour() { return endHour; }
        public void setEndHour(Integer endHour) { this.endHour = endHour; }

        public Integer getSlotDurationMinutes() { return slotDurationMinutes; }
        public void setSlotDurationMinutes(Integer slotDurationMinutes) { this.slotDurationMinutes = slotDurationMinutes; }
    }
}
