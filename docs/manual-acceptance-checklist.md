# Manual Acceptance Checklist

## Application Flow

- Company publishes a verified active job.
- Job seeker sees the job in discovery.
- Job seeker applies once; duplicate application is blocked.
- Application appears in job seeker applications.
- Application appears in company applicants.
- Company receives `NEW_APPLICATION` notification.
- Company advances status and schedules interview.
- Job seeker sees updated status and explicit interview date/time/timezone.
- Job seeker receives status/interview notification.

## Verification Flow

- Company submits verification.
- Admin sees pending company.
- Admin approves or rejects.
- Company receives verification notification.
- Rejected company can edit and resubmit.
- Pending/rejected company cannot publish active jobs.

## Moderation Flow

- Admin removes a job.
- Job disappears from discovery.
- Existing applications remain visible.
- Company receives moderation notification.
- Admin restores job.

## Account Status

- Admin suspends a user.
- Active session is re-evaluated.
- Protected routes become inaccessible.
- Reactivation restores role access.

## Messaging

- FCM token refresh registers only the current user/device.
- Invalid payload types are ignored.
- No raw route strings are trusted.
- Remote push sending is tested only with trusted backend/Cloud Function infrastructure.
