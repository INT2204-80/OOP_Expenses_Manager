Add-Type -AssemblyName System.Drawing
$path = 'c:\Users\RAZER\Downloads\OOP_Expenses_Manager\src\main\resources\images\icon.png'
$bmp = New-Object System.Drawing.Bitmap($path)

for ($y = 0; $y -lt $bmp.Height; $y++) {
    for ($x = 0; $x -lt $bmp.Width; $x++) {
        $pixel = $bmp.GetPixel($x, $y)
        if ($pixel.A -gt 0) {
            $newColor = [System.Drawing.Color]::FromArgb($pixel.A, 0, 0, 0)
            $bmp.SetPixel($x, $y, $newColor)
        }
    }
}
$bmp.Save('c:\Users\RAZER\Downloads\OOP_Expenses_Manager\src\main\resources\images\icon_black.png', [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
