import { apiFetch } from "./api.js";

// GET CURRENT USER

// Gọi API /api/auth/me để lấy thông tin người dùng hiện tại
// JWT được lưu trong HttpOnly Cookie nên JavaScript không đọc trực tiếp được.
// Browser sẽ tự động gửi Cookie kèm request.

export async function getCurrentUser() {

    try {

        // Gửi GET request tới API /api/auth/me
        const response = await apiFetch("/api/auth/me", {
            // HTTP method là GET
            method: "GET",
            // Khong can credentials : "include" khong can , vi trong apiFetch da co san
        });


        // Kiểm tra HTTP response
        // response.ok = true khi status code nằm trong khoảng 200–299
        // Nếu false:
        // → chưa đăng nhập
        // → hoặc JWT không hợp lệ / hết hạn
        if (!response.ok) {
            // Không có user
            return null;
        }


        // API trả về UserDTO dưới dạng JSON
        // Ví dụ:
        // {
        //     "id": 1,
        //     "username": "chantaro",
        //     "roles": [
        //         "ROLE_ADMIN",
        //         "ROLE_STAFF"
        //     ]
        // }
        // response.json() chuyển JSON từ server
        // thành JavaScript object.

        return await response.json();


    } catch (error) {

        // Nếu xảy ra lỗi trong quá trình gọi API
        // Ví dụ: server không chạy, network error...
        console.error("Error getting current user:", error);

        // Trả về null để code gọi getCurrentUser()
        // biết rằng không lấy được thông tin user.
        return null;
    }
}


// LOGOUT

 export async function logout() {

    try {

        const response = await fetch("/api/auth/logout", {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok) {
            return null;
        }

        return await response.text();

    } catch (error) {
        console.error("Error to logout:", error);
        return null;
    }
 }