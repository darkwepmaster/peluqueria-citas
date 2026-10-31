using PeluqueriaRecordatorios;

var builder = Host.CreateApplicationBuilder(args);
builder.Services.AddHttpClient<ApiCliente>(c => c.BaseAddress = new Uri(builder.Configuration["Api:BaseUrl"] ?? "http://localhost:8080/"));
builder.Services.AddSingleton<IMensajero, MensajeroCorreo>();
builder.Services.AddSingleton<IMensajero, MensajeroWhatsApp>();
builder.Services.AddHostedService<Despachador>();
builder.Build().Run();
