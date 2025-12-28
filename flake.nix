{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };
  outputs =
    { nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        devShells.default = pkgs.mkShell {
          nativeBuildInputs = with pkgs; [
            stdenv.cc.cc.lib
            jdk21
            libglvnd
            xorg.libX11
            python312
            nodejs_20
          ];
          LD_LIBRARY_PATH = ''${pkgs.stdenv.cc.cc.lib}/lib:${pkgs.libglvnd}/lib:${pkgs.xorg.libX11}/lib'';
        };
      }
    );
}
