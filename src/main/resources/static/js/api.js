export async function apiFetch(url, options = {}) {

    // 1. Gọi API mà frontend đang muốn gọi
    const response = await fetch(url, {
        ...options,

        // Cho phép Browser gửi Cookie kèm theo request
        // → accessToken Cookie sẽ được gửi lên Backend
        credentials: "include"
    });


    // 2. Kiểm tra Access Token có vấn đề không
    // 401 = Request chưa được xác thực hợp lệ
    if (response.status === 401) {

        // Đọc JSON mà Backend trả về
        const data = await response.json();

        // Kiểm tra lý do Backend trả về 401
        if (data.message === "ACCESS_TOKEN_EXPIRED") {

            // Access Token đã hết hạn
            //console.log("Access Token expired");

            // 3. Access Token hết hạn
            // → Gọi API refresh để xin Access Token mới
            const refreshResponse = await fetch("/api/auth/refresh", {
                method: "POST",

                // Gửi Refresh Token Cookie lên Backend
                credentials: "include"
            });


            // Kiểm tra kết quả của API refresh
            console.log(refreshResponse.status);


            // 4. Refresh thành công
            // Backend đã tạo Access Token mới
            // và gửi Access Token mới về bằng Cookie
            if (refreshResponse.ok) {

                // 5. Gọi lại API ban đầu thêm một lần nữa
                // Lúc này Browser sẽ gửi Access Token mới
                return await fetch(url, {
                    ...options,
                    credentials: "include"
                });
            }
        }


        // Access Token không hợp lệ
        if (data.message === "INVALID_ACCESS_TOKEN") {

            // Không refresh
            console.log("Invalid Access Token");
        }
    }


    // 6. Nếu API ban đầu không bị 401
    // → trả luôn kết quả cho nơi gọi apiFetch()
    return response;
}