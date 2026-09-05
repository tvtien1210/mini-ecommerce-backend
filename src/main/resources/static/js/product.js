// PAGE LOAD

// Chờ HTML load xong
document.addEventListener("DOMContentLoaded", function () {

    // Đăng ký Event Listener cho các nút Add to Cart
    setupAddToCartEvents();

});


// ADD TO CART EVENTS

// Đăng ký sự kiện click cho các nút Add to Cart
function setupAddToCartEvents() {

    // Lấy tất cả button có class "add-to-cart-btn"
    const addToCartButtons =
        document.querySelectorAll(".add-to-cart-btn");


    // Duyệt qua từng button
    addToCartButtons.forEach(function (button) {

        // Đăng ký sự kiện click
        button.addEventListener("click", function () {


            const productId =
                Number(button.dataset.productId);


            // Mặc định khi User click
            // sẽ thêm 1 sản phẩm vào Cart
            const quantity = 1;


            // Gọi function addToCart()
            addToCart(
                productId,
                quantity
            );

        });

    });

}


// ADD TO CART

// Gọi Backend API để thêm Product vào Cart
async function addToCart(
    productId,
    quantity
) {

    try {

        // Kiểm tra dữ liệu trước khi gọi API
        console.log(
            "Adding product to cart:",
            productId,
            "quantity:",
            quantity
        );


        // CALL BACKEND API

        const response =
            await apiFetch(
                "/api/cart/items",
                {

                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        productId: productId,
                        quantity: quantity
                    })

                }
            );


        // Nếu apiFetch không trả response
        // thì dừng
        if (!response) {
            return;
        }


        // CHECK RESPONSE

        // Kiểm tra HTTP status
        // Ví dụ:
        // 200 / 201
        // response.ok = true
        // 400 / 401 / 403 / 500
        // response.ok = false
        if (!response.ok) {

            const errorData = await response.json();

            console.error("Add to cart failed",errorData);

            showToast(errorData.message, "warning");

            return;

        }


        // ==================================================
        // GET UPDATED CART
        // ==================================================

        // Backend trả về CartDTO
        //
        // Ví dụ:
        //
        // {
        //     id: 1,
        //     cartItems: [...],
        //     totalPrice: 50000000,
        //     currency: "VND"
        // }
        const cart =
            await response.json();


        console.log(
            "Cart after adding product:",
            cart
        );


        // Cập nhật số trên Cart Badge
        updateCartBadge(cart);


        // ==================================================
        // SUCCESS
        // ==================================================

        // Tạm thời chỉ thông báo thành công
        console.log(
            "Product added to cart successfully"
        );


        showToast(
            "Product added to cart!","success"
        );


    } catch (error) {

        // Nếu API lỗi,mat ket noi internet, function loi
        // hiển thị lỗi trong Console
        console.error(error);

        //Thong bao ra man hinh
        showToast(
            "Something went wrong. Please try again.","warning"
        );



    }

}




//FUNCTION SHOW TOAST

//Hien thi Toast voi noi dung message duoc truyen vao

function showToast(message, type = "success") {

    // Lấy Toast element
    const toastElement =
        document.getElementById("toast");

    // Lấy element chứa nội dung message
    const toastMessageElement =
        document.getElementById("toastMessage");


    // Xóa màu cũ
    toastElement.classList.remove(
        "text-bg-success",
        "text-bg-danger",
        "text-bg-warning"
    );


    // Thêm màu tương ứng với loại message
    if (type === "success") {

        toastElement.classList.add(
            "text-bg-success"
        );

    } else if (type === "danger") {

        toastElement.classList.add(
            "text-bg-danger"
        );

    } else if (type === "warning") {

        toastElement.classList.add(
            "text-bg-warning"
        );
    }


    // Đưa message vào Toast
    toastMessageElement.textContent = message;


    // Lấy hoặc tạo Bootstrap Toast instance
    const toast =
        bootstrap.Toast.getOrCreateInstance(
            toastElement
        );


    // Hiển thị Toast
    toast.show();
}

