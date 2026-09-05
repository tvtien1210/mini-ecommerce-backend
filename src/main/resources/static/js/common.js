// ==================================================
// apiFetch()
// ==================================================

async function apiFetch(url, options = {}) {

    // ==================================================
    // 1. GỌI API BAN ĐẦU
    // ==================================================

    let response = await fetch(url, {
        ...options,
        credentials: "include"
    });


    // ==================================================
    // 2. ACCESS TOKEN CÓ VẤN ĐỀ
    // ==================================================

    if (response.status === 401) {

        const data = await response.json();


        // ==================================================
        // 3. ACCESS TOKEN HẾT HẠN
        // ==================================================

        if (data.message === "ACCESS_TOKEN_EXPIRED") {

            console.log("Access Token expired");


            // ==================================================
            // 4. REFRESH ACCESS TOKEN
            // ==================================================

            const refreshResponse =
                await fetch("/api/auth/refresh", {
                    method: "POST",
                    credentials: "include"
                });


            console.log(
                "Refresh:",
                refreshResponse.status
            );


            // ==================================================
            // 5. REFRESH THÀNH CÔNG
            // ==================================================

            if (refreshResponse.ok) {

                console.log(
                    "Refresh successful"
                );


                // ==================================================
                // 6. GỌI LẠI API BAN ĐẦU
                // ==================================================

                response = await fetch(url, {
                    ...options,
                    credentials: "include"
                });


                return response;
            }


            // Refresh thất bại
            window.location.href = "/login";

            return;
        }


        // ==================================================
        // 7. ACCESS TOKEN KHÔNG HỢP LỆ
        // ==================================================


        //Giả sử access token bị sửa:
        //accessToken=abc123_fake
        //hoặc signature không hợp lệ.

        if (data.message === "INVALID_ACCESS_TOKEN") {

            console.log(
                "Invalid Access Token"
            );

            window.location.href = "/login";

            return;
        }


        // ==================================================
        // 8. USER CHƯA ĐĂNG NHẬP
        // ==================================================

        if (data.message === "UNAUTHENTICATED") {

            console.log(
                "User is not authenticated"
            );
            console.log(response);

            // KHONG REDIRECT
            // Trả response về cho nơi gọi API xử lý
            return response;
        }
    }


    // ==================================================
    // 9. API KHÔNG BỊ 401
    // ==================================================

    return response;
}


// ==================================================
// getCurrentUser()
// ==================================================

// Gọi API /api/auth/me để lấy thông tin người dùng hiện tại
// JWT được lưu trong HttpOnly Cookie nên JavaScript không đọc trực tiếp được.
// Browser sẽ tự động gửi Cookie kèm request.

async function getCurrentUser() {

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

        const currentUser = await response.json();

        console.log("expriresAt: ", currentUser.expiresAt);


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

        return currentUser;


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

 async function logout() {

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

// ==================================================
// updateCartBadge()
// ==================================================
function updateCartBadge(cart) {

    // Lấy badge trên Navbar
    const cartBadge =
        document.getElementById("cartBadge");

    // Nếu không tìm thấy badge thì dừng
    if (!cartBadge) {
        return;
    }

    // Tính tổng quantity của tất cả CartItem
    const totalQuantity =
        cart.cartItems.reduce(
            function (total, item) {
                return total + item.quantity;
            },
            0
        );

    // Hiển thị số lượng lên badge
    cartBadge.textContent =
        totalQuantity;
}