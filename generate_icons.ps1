Add-Type -AssemblyName System.Drawing

function Create-Icons {
    param(
        [string]$sourcePath,
        [int]$cropX,
        [int]$cropY,
        [int]$cropSize,
        [string]$outputDirBase
    )

    $src = [System.Drawing.Bitmap]::FromFile($sourcePath)
    $cropped = New-Object System.Drawing.Bitmap($cropSize, $cropSize)
    $gCrop = [System.Drawing.Graphics]::FromImage($cropped)
    $gCrop.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $gCrop.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $gCrop.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    
    $srcRect = New-Object System.Drawing.Rectangle($cropX, $cropY, $cropSize, $cropSize)
    $destRect = New-Object System.Drawing.Rectangle(0, 0, $cropSize, $cropSize)
    $gCrop.DrawImage($src, $destRect, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
    $gCrop.Dispose()
    $src.Dispose()

    $densities = @{
        "mipmap-mdpi"    = 48
        "mipmap-hdpi"    = 72
        "mipmap-xhdpi"   = 96
        "mipmap-xxhdpi"  = 144
        "mipmap-xxxhdpi" = 192
    }

    foreach ($folder in $densities.Keys) {
        $size = $densities[$folder]
        $targetDir = Join-Path $outputDirBase $folder
        if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force | Out-Null }

        # 1. Square Icon
        $squareBmp = New-Object System.Drawing.Bitmap($size, $size)
        $gSquare = [System.Drawing.Graphics]::FromImage($squareBmp)
        $gSquare.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $gSquare.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $gSquare.DrawImage($cropped, 0, 0, $size, $size)
        $gSquare.Dispose()
        $squareBmp.Save((Join-Path $targetDir "ic_launcher.png"), [System.Drawing.Imaging.ImageFormat]::Png)
        $squareBmp.Dispose()

        # 2. Round Icon (Circular mask)
        $roundBmp = New-Object System.Drawing.Bitmap($size, $size)
        $gRound = [System.Drawing.Graphics]::FromImage($roundBmp)
        $gRound.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $gRound.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        
        $path = New-Object System.Drawing.Drawing2D.GraphicsPath
        $path.AddEllipse(0, 0, $size, $size)
        $gRound.SetClip($path)
        $gRound.DrawImage($cropped, 0, 0, $size, $size)
        $gRound.ResetClip()

        # Add subtle soft border
        $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(60, 255, 255, 255), 2)
        $gRound.DrawEllipse($pen, 1, 1, $size - 2, $size - 2)
        $pen.Dispose()

        $gRound.Dispose()
        $roundBmp.Save((Join-Path $targetDir "ic_launcher_round.png"), [System.Drawing.Imaging.ImageFormat]::Png)
        $roundBmp.Dispose()
    }

    $cropped.Dispose()
    Write-Output "Generated icons for $outputDirBase"
}

# Boy's photo goes to girl's flavor (so her app "My Boy" has his face!)
$boyPhoto = "C:/Users/USER/.gemini/antigravity/brain/20d110c8-8223-4153-93ea-82addf0b250d/.user_uploaded/media_1789181122682.jpg"
Create-Icons -sourcePath $boyPhoto -cropX 104 -cropY 160 -cropSize 560 -outputDirBase "c:\Users\USER\Documents\wa\lovelink\app\src\girl\res"

# Girl's photo goes to boy's flavor (so his app "My Girl" has her face!)
$girlPhoto = "C:/Users/USER/.gemini/antigravity/brain/20d110c8-8223-4153-93ea-82addf0b250d/.user_uploaded/media_1789181122681.jpg"
Create-Icons -sourcePath $girlPhoto -cropX 20 -cropY 240 -cropSize 540 -outputDirBase "c:\Users\USER\Documents\wa\lovelink\app\src\boy\res"
