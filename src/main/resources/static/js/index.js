$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");

    // const token = $("meta[name='_csrf']").attr("content");
    // const header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e, xhr, options) {
    //     xhr.setRequestHeader(header, token);
    // });

    const title = $("#recipient-name").val();
    const content = $("#message-text").val();
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content},
        function (data) {
            data = $.parseJSON(data);
            $("#hintBody").text(data.msg);

            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                if (data.code === 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    );

}