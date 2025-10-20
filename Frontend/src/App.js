import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import LandlordSignUp from "./pages/Landlord/LandlordAuth/LandlordSignUp";
import LandlordDashboard from "./pages/Landlord/LandlordDashboard";
import MyListings from "./pages/Landlord/MyListings/MyListings";
import AddListing from "./pages/Landlord/AddListings/AddListing";
import AssignAccommodation from "./pages/Landlord/AssignAccommodation/AssignAccommodation";
import ApplicationsRequests from "./pages/Landlord/ApplicationRequests/ApplicationRequests";
import LandlordProfile from "./pages/Landlord/LndlordProfile";
import EditProfile from "./pages/Landlord/EditProfile";
import AdminDashboard from "./pages/Administrator/Dashboard/Dashboard";
import AdminSignUp from "./pages/Administrator/AdminSignUp/AdminSignUp";
import VerifyLandlords from "./pages/Administrator/VerifyLandlords/VerifyLandlords";
import VerifyListings from "./pages/Administrator/VerifyListings/VerifyListings";

import ProtectedRoute from "./components/ProtectedRoute";

import LandingPage from "./pages/Shared/LandingPage";
import LoginPage from "./pages/Shared/Login";
import SignUpPage from "./pages/Shared/SignUp";
import LogoutPage from "./pages/Shared/Logout";

import SearchAccommodation from "./pages/Student/SearchAccommodation/SearchAccommodation";
import AccommodationDetails from "./pages/Student/AccomodationDetails/AccommodationDetails";
import MyApplications from "./pages/Student/MyApplications/MyApplications";
import SubmitReview from "./pages/Student/SubmitReview/SubmitReview";
import StudentSignUp from "./pages/Student/StudentSignUp/StudentSignUp";
import StudentDashboard from "./pages/Student/Dashboard/StudentDashboard";
import StudentProfile from "./pages/Student/Profile/StudentProfile";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route
            path="/student/login"
            element={<LoginPage defaultRole="student" />}
        />
        <Route
            path="/landlord/login"
            element={<LoginPage defaultRole="landlord" />}
        />
        <Route
            path="/admin/login"
            element={<LoginPage defaultRole="admin" />}
        />
        <Route path="/signup" element={<SignUpPage />} />
          <Route path="/student/signup" element={<StudentSignUp />} />
          <Route path="/logout" element={<LogoutPage />} />


        {/* Landlord routes */}
        <Route path="/landlord/signup" element={<LandlordSignUp />} />
        <Route path="/landlord-profile" element={<LandlordProfile />} />
        <Route path="/landlord/dashboard" element={<LandlordDashboard />} />
        <Route path="/my-listings" element={<MyListings />} />
        <Route path="/add-listing" element={<AddListing />} />
        <Route path="/applications-requests" element={<ApplicationsRequests />} />
        <Route path="/assign-accommodation" element={<AssignAccommodation />} />
        <Route path="/edit-profile" element={<EditProfile />} />


        {/* Administrator routes */}
        <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute allowedRoles={["admin"]}>
                <AdminDashboard />
              </ProtectedRoute>
            }
        />
          <Route path="/admin/signup" element={<AdminSignUp />} />

          <Route
            path="/admin/verify-landlords"
            element={
              <ProtectedRoute allowedRoles={["admin"]}>
                <VerifyLandlords />
              </ProtectedRoute>
            }
        />
        <Route
            path="/admin/verify-listings"
            element={
              <ProtectedRoute allowedRoles={["admin"]}>
                <VerifyListings />
              </ProtectedRoute>
            }
        />

        {/* Student routes */}
        <Route
            path="/student/dashboard"
            element={
                <ProtectedRoute allowedRoles={["student"]}>
                    <StudentDashboard />
                </ProtectedRoute>
            }
        />
          <Route
              path="/student/search"
              element={
                  <ProtectedRoute allowedRoles={["student"]}>
                      <SearchAccommodation />
                  </ProtectedRoute>
            }
        />
        <Route
            path="/student/accommodation/:id"
            element={
              <ProtectedRoute allowedRoles={["student"]}>
                <AccommodationDetails />
              </ProtectedRoute>
            }
        />
        <Route
            path="/student/applications"
            element={
              <ProtectedRoute allowedRoles={["student"]}>
                <MyApplications />
              </ProtectedRoute>
            }
        />
        <Route
            path="/student/review"
            element={
              <ProtectedRoute allowedRoles={["student"]}>
                <SubmitReview />
              </ProtectedRoute>
            }
        />
          <Route
              path="/student/profile"
              element={
                  <ProtectedRoute allowedRoles={["student"]}>
                      <StudentProfile />
                  </ProtectedRoute>
              }
          />

      </Routes>
    </Router>
  );
}

export default App;
